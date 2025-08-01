package com.bank.web.app.api.gatway.ApiGatway.Security;

import com.bank.web.app.api.gatway.ApiGatway.Redis.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
//import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@Order(-1)
public class TokenRefreshFilter implements GlobalFilter {
    private final WebClient webClient = WebClient.create();

    @Autowired
    private RedisService service;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public String getEndpointUrl() {
        return keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    public Mono<Boolean> matchToken(String id, String refresh_token, String access_token) {
        return service.getField(id, "refresh_token")
                .flatMap(refreshToken ->
                        service.getField(id, "access_token")
                                .map(accessToken -> {
                                    return refresh_token.equals(refreshToken) && access_token.equals(accessToken);
                                })
                )
                .defaultIfEmpty(false); // in case any field is missing
    }
//
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
////        service.saveRefreshToken("user1","2342342342b4234242434", 10000).subscribe(success -> System.out.println("Saved to Redis: " + success));
//
//        if(authHeader != null && authHeader.startsWith("Bearer ")){
//            String token = authHeader.substring(7);
//
//            try{
//                SignedJWT jwt = SignedJWT.parse(token);
//                Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
//                if(expirationTime != null && expirationTime.before(new Date())){
//                    System.err.println(">>>> Token expired");
//
////                    String refreshToken = exchange.getRequest().getHeaders().getFirst("Refreshtoken")
//                    String id = jwt.getJWTClaimsSet().getStringClaim("sub");
//                    String refreshToken = service.getRefreshToken("refresh:"+id).block();
//                    System.err.println(refreshToken);
////                    String refreshToken = "eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0NjFlNzg5NS1jMjk2LTQ3OGQtYmJkNC02OGI0ZjUwNzU2MTAifQ.eyJleHAiOjE3NTE1MzM3NzIsImlhdCI6MTc1MTUzMTk3MiwianRpIjoiN2IyMDRlMDQtMzkwNi00ZmY0LTkxYzktYjRkZDAyZjI4YzU1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDk4L3JlYWxtcy9iYW5rLXdlYi1hcHAiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjkwOTgvcmVhbG1zL2Jhbmstd2ViLWFwcCIsInN1YiI6ImNjMThkNGNkLTYyYTUtNGU3Yy1hOThiLTc3ZjlhN2JlM2FiMyIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJiYW5rLXdlYi1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiMWRkYTYwNTItMDBjYS00MjkzLTliMDItOTA4MjU5YmFjODZlIiwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiMWRkYTYwNTItMDBjYS00MjkzLTliMDItOTA4MjU5YmFjODZlIn0.DEYtWPutzY_uuOVXVk52Rp8AVwTSI4w2AAw9rzeDgUQr8g86u5CcYIcmW1VnK_F2zT3h_T4WO28EOMVzNO7ueg";
//                    if(refreshToken != null){
//                        return refreshAccessToken(refreshToken)
//                                .flatMap(newAccessToken ->{
//                                    exchange.getRequest().mutate()
//                                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + newAccessToken)
//                                            .build();
//                                    return chain.filter(exchange);
//                                });
//                    }
//                }
//            } catch (ParseException e) {
//                log.info("ParseException :: {}",e.getMessage());
//            }
//        }
//        return chain.filter(exchange);
//    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    //    log.info("url:{} cleint:{} relem:{} secret:{}",keycloakServerUrl,clientId,realm,clientSecret);
    
    
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
    
            try {
                SignedJWT jwt = SignedJWT.parse(token);
                Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
                log.info(">>> Token expired at: {}", expirationTime);
    
                if (expirationTime != null && expirationTime.before(new Date())) {
                    log.warn(">>> Token expired at: {}", expirationTime);
                    System.err.println("Token Expired");
    
                    // Get user ID from token
                    String userId = jwt.getJWTClaimsSet().getStringClaim("sub");
    //                String redisKey = "refresh:" + userId;
    
    
                    // Check Redis for refresh token
                    return service.getField(userId,"refresh_token")
                            .flatMap(refreshToken -> {
                                log.warn("INSIDE METHOD Refresh token :: {}",refreshToken);
                                if (refreshToken == null || refreshToken.isEmpty()) {
                                    log.warn(">>> No refresh token found in Redis for user: {}", userId);
                                     exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                     return exchange.getResponse().setComplete();
                                }
    //                            matchToken(userId, refreshToken, token)
    //                                    .flatMap(result -> {
    //                                        if (result) {
    //                                            log.info("Both tokens are valid");
    //                                            return Mono.just("");
    //
    //                                        } else {
    //                                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    //                                            log.info("Token not Match");
    //                                            return exchange.getResponse().setComplete();// ✅ safe and correct
    //                                        }
    //                                    }).subscribe();
    
                                return matchToken(userId, refreshToken, token)
                                        .flatMap(result -> {
                                            if (!result) {
                                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                                log.info("Token not Match");
                                                return exchange.getResponse().setComplete(); // ✅ Return early on mismatch
                                            }
    
                                            log.info("Token match");
                                            // Call Keycloak to refresh access token
                                            try {
                                                return refreshAccessToken(refreshToken, userId, token)
                                                        .flatMap(newAccessToken -> {
                                                            log.info(">>> Injecting new access token into request");
    
                                                            // Mutate request with new Authorization header
                                                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                                                                    .build();
    
                                                            ServerWebExchange mutatedExchange = exchange.mutate()
                                                                    .request(mutatedRequest)
                                                                    .build();
    
                                                            return chain.filter(mutatedExchange);
                                                        }).onErrorResume(ex -> {
                                                            log.warn("Token refresh failed: {}", ex.getMessage());
                                                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                                            return exchange.getResponse().setComplete();
                                                        });
    
                                            } catch (ParseException e) {
                                                log.error("ParseException while refreshing token: {}", e.getMessage());
                                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                                return exchange.getResponse().setComplete();
                                            }
                                        });
                            }).switchIfEmpty(Mono.defer(() -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    })); // Redis didn't return anything
                }
    
            } catch (ParseException e) {
                log.error(">>> Failed to parse JWT: {}", e.getMessage());
            }
        }

    // Token not present or not expired → proceed as-is
    return chain.filter(exchange);
}


    private Mono<String> refreshAccessToken(String refreshToken,String userId,String token) throws ParseException {
//      Check both token match or not
        log.warn("calling... refreshToken");
        log.info("URL:: {}",getEndpointUrl());
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret); // optional if public client
        formData.add("refresh_token", refreshToken);
//        SignedJWT jwt = SignedJWT.parse();
        SignedJWT jwt = SignedJWT.parse(refreshToken);
        Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
        if(expirationTime.before(new Date())){
            log.info("RefreshAccessToken:: Refresh Token Expired inside {} ",expirationTime);
            return Mono.error(new RuntimeException("Refresh token expired"));
        }
        else {
            log.info("Token Still valid");
        }

        try {
            return webClient.post()
//                    .uri("http://localhost:9098/realms/bank-web-app/protocol/openid-connect/token")
                    .uri(getEndpointUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        System.err.println(">>> New Token received...");
                        try {
                            return extractAccessTokenFromJson(response, userId);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }catch (Exception e){
            log.error("Failed to both token expired {}",e.toString());
            return Mono.just("");
        }
    }


    private String extractAccessTokenFromJson(String response,String userId) throws Exception {
        // Use proper JSON parser here like Jackson or org.json
        // Dummy implementation:
//        System.err.println(response);
//        log.warn(response);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            String accessToken = rootNode.path("access_token").asText();
            String refreshToken = rootNode.path("refresh_token").asText();
            Map<String,String> map= new HashMap<>();
            map.put("access_token",accessToken);
            map.put("refresh_token",refreshToken);

            service.saveRefreshToken(userId, map, 21600).subscribe(v -> {
                if (v) {
                    log.info("redis update success");
                } else {
                    log.info("redis update failed");
                }
            });
            System.err.println("New Token : "+accessToken);
            return accessToken;
        }catch (Exception e){
            log.warn("extractAccessTokenFromJson :: {}",e.getMessage());
            return "";
        }
    }
}
