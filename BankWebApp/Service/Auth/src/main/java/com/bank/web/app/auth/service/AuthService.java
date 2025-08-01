package com.bank.web.app.auth.service;

import com.bank.web.app.auth.dto.*;
import com.bank.web.app.auth.kafka.AuthDto;
import com.bank.web.app.auth.kafka.AuthLinkProducer;
import com.bank.web.app.auth.kafka.RedisTokenDto;
import com.bank.web.app.auth.model.UserVerification;
import com.bank.web.app.auth.repo.VerificationRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthLinkProducer authLinkProducer;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.admin-username}")
    private String adminUsername;
    @Value("${keycloak.admin-password}")
    private String adminPassword;

    public ResponseDTO registerUser(RegisterRequest request) {
        String adminToken  = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create user
        Map<String,Object> user = new HashMap<>();
        user.put("username",request.getUsername());
        user.put("enabled",true);
        user.put("email",request.getEmail());
        user.put("firstName", request.getFirstName());  // <-- Add this
        user.put("lastName", request.getLastName());
//        user.put("contact",request.getContact());

        HttpEntity<Map<String,Object>> createUserRequest= new HttpEntity<>(user,headers);
            ResponseEntity<?> createResponse = restTemplate.postForEntity(
                    keycloakServerUrl + "/admin/realms/" + realm + "/users",
                    createUserRequest,
                    Void.class
            );

        if (!createResponse.getStatusCode().is2xxSuccessful()){
            return new ResponseDTO("401","User registration fail", false);
        }
        // Fetch created user ID
        ResponseEntity<UserRepresentation[]> response = restTemplate.exchange(
                keycloakServerUrl + "/admin/realms/" + realm + "/users?username=" + request.getUsername(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserRepresentation[].class
        );
        String userId = Objects.requireNonNull(response.getBody())[0].getId();
        System.err.println(userId);
        try{
            AuthDto authDto = new AuthDto();
            String URL = generateUrl(userId,request.getEmail());
            authDto.setUrl(URL);
            authDto.setEmail(request.getEmail());
            authDto.setFname(request.getFirstName());
            authLinkProducer.sendAuthLink(authDto);
        }catch (MessagingException exception){
            log.info("Exception :: {}",exception.getMessage());
        }

        Map<String, Object> passwordPayload = Map.of(
                "type", "password",
                "value", request.getPassword(),
                "temporary", false
        );
        HttpEntity<Object> passwordRequest = new HttpEntity<>(passwordPayload, headers);

        restTemplate.put(
                keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password",
                passwordRequest
        );

        return new ResponseDTO("201","User registration Success",true);

    }


    private String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=admin-cli" +
                "&username=" + adminUsername +
                "&password=" + adminPassword +
                "&grant_type=password";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(
                keycloakServerUrl + "/realms/master/protocol/openid-connect/token",
                HttpMethod.POST,
                request,
                TokenResponse.class
        );

        return response.getBody().getAccess_token();
    }


    public ResponseDTO login(LoginRequest request) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", request.getUsername());
            body.add("password", request.getPassword());
            body.add("grant_type", "password");



            HttpEntity<MultiValueMap<String, String>> loginRequest = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    keycloakServerUrl + "/realms/bank-web-app/protocol/openid-connect/token",
                    HttpMethod.POST,
                    loginRequest,
                    Map.class
            );

            Map<String,String> obj = new HashMap<>();
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                String RefreshToken = (String) response.getBody().get("refresh_token");
//                todo-produce refresh token
                SignedJWT signedJWT = SignedJWT.parse(accessToken);
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                String id = claims.getStringClaim("sub");
                log.info("SUB ID :: {}",id);

                RedisTokenDto data = new RedisTokenDto();
                data.setId(id);
                data.setRefresh_token(RefreshToken);
                data.setAccess_token(accessToken);
                authLinkProducer.sendRefreshToken(data);

                obj.put("token",accessToken);
//    todo            obj.put("refresh_token",RefreshToken);

                return new ResponseDTO("200", "Login Success", obj);
            }
            obj.put("token","");
//    todo        obj.put("refresh_token","");
            return new ResponseDTO("fail", "Login failed", obj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;

        }
    }

    @Autowired
    private VerificationRepo repo;

    public String generateUrl(String id,String email){
            String token = UUID.randomUUID().toString();
            UserVerification userVerification = new UserVerification();
            userVerification.setUserId(id);
            userVerification.setVerified(false);
            userVerification.setExpiryDate(LocalDateTime.now().plusHours(24));
            userVerification.setToken(token);
            userVerification.setEmail(email);
            userVerification.setSendAt(LocalDateTime.now());
            repo.save(userVerification);
            return "http://localhost:8222/api/v1/auth/verify/" + id +"/"+ token;


    }

    public ResponseDTO verify(String id, String token) {
        Optional<UserVerification> user = repo.findById(id);
        if(user.isEmpty()){
            return new ResponseDTO("404","invalid url",null);
        }
        if(user.get().isVerified()){
            return new ResponseDTO("200","User is already verified.",null);
        }
        if(user.get().getExpiryDate().isBefore(LocalDateTime.now())){
            if(ResendVerificationToken(id,user.get().getEmail())) {
                return new ResponseDTO("200", "link expired..", null);
            }
        }
        String authToken = loginAdmin();
        log.warn(authToken);

        RestTemplate restTemplate = new RestTemplate();
        String Url = keycloakServerUrl+"/admin/realms/"+realm+"/users/"+id;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("emailVerified", true);
            if(user.get().getToken().equalsIgnoreCase(token)){
                if(user.get().getExpiryDate().isAfter(LocalDateTime.now())){
                        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                        ResponseEntity<Void> response = restTemplate.exchange(Url, HttpMethod.PUT, request, Void.class);
                         if (response.getStatusCode().is2xxSuccessful()) {
                             user.get().setVerified(true);
                             repo.save(user.get());
                            return new ResponseDTO(response.getStatusCode().toString(),"email verified",null);
                         } else {
                            return new ResponseDTO(response.getStatusCode().toString(),"email verification failed",null);
                    }
            }
        }
            return new ResponseDTO("","",null);
    }

    public boolean ResendVerificationToken(String id,String email){
        String token = UUID.randomUUID().toString();
        var userVerification = repo.findById(id);
        userVerification.get().setToken(token);
        userVerification.get().setSendAt(LocalDateTime.now());
        userVerification.get().setExpiryDate(LocalDateTime.now().plusHours(24));
        repo.save(userVerification.get());

        try{
            AuthDto authDto = new AuthDto();
            String URL = "http://localhost:8222/api/v1/auth/verify/" + id +"/"+ userVerification.get().getToken();
            authDto.setUrl(URL);
            authDto.setEmail(email);
            authDto.setFname("");
            authLinkProducer.sendAuthLink(authDto);
        }catch (MessagingException exception){
            log.info("Resend Exception :: {}",exception.getMessage());
        }
        return true;


    }



    public String loginAdmin() {

        try {
            RestTemplate restTemplate = new RestTemplate();
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // Set form parameters
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("username",adminUsername);
            form.add("password",adminPassword);
            form.add("grant_type", "password");
            form.add("client_id", "admin-cli");
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

            // Send POST request
            String url = keycloakServerUrl+"/realms/master/protocol/openid-connect/token";
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    request,
                    String.class
            );
//            System.out.println("Response: " + response.getBody());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("access_token").asText();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;

        }
    }
}
