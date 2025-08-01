package com.bank.web.app.auth.controller;

import com.bank.web.app.auth.dto.LoginRequest;
import com.bank.web.app.auth.dto.RegisterRequest;
import com.bank.web.app.auth.dto.ResponseDTO;
import com.bank.web.app.auth.service.AuthService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.RequestNotExecutedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth/")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("register")
    public ResponseDTO register(@Valid @RequestBody RegisterRequest registerRequest, Errors errors){
        log.info("Request received");
        if(errors.hasErrors()){
            return new ResponseDTO("200",errors.getAllErrors().get(0).getDefaultMessage(),null);
        }
        return authService.registerUser(registerRequest);
    }
    @PostMapping("login")
    @CircuitBreaker(name = "authservice",fallbackMethod = "loginFallBack")
    public ResponseDTO login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    public ResponseDTO loginFallBack(LoginRequest loginRequest, Throwable ex) {
        return new ResponseDTO("503","Service Unavailable",null);
    }

    @GetMapping("verify/{id}/{token}")
    @RateLimiter(name = "verifyRateLimiter",fallbackMethod = "verifyFallBack")
    public ResponseEntity<ResponseDTO> verify(@PathVariable String id,@PathVariable String token) {
//        return authService.verify(id,token);
        return ResponseEntity.ok(authService.verify(id, token));
    }

//
public ResponseEntity<ResponseDTO> verifyFallBack(String id, String token, Throwable ex) {
    log.warn("Rate limit or service issue: {}", ex.getMessage());
    ResponseDTO response = new ResponseDTO("503", "Service Unavailable", null);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
}



    @GetMapping("test")
    public ResponseDTO testToken(@RequestHeader("Authorization") String authHeader) throws ParseException {
        String token = authHeader.replaceFirst("(?i)^Bearer ", "");
      SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String username = claims.getStringClaim("preferred_username");
        Map<String, Object> realmAccess = (Map<String, Object>) claims.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");
        System.out.println("Roles:");
        roles.forEach(System.out::println);

        String email = claims.getStringClaim("email");
        log.info("Details :: username::{} email::{}",username,email);
        return new ResponseDTO("200","Token",authHeader);

    }
    @GetMapping("   heartbeat")
    public ResponseEntity<String> heartbeat() {
        log.info("Auth service Health check");
        return ResponseEntity.ok("ALIVE");
    }




}
