package com.bank.web.app.api.gatway.ApiGatway.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        String jwkSetUri = "http://localhost:9098/realms/bank-web-app/protocol/openid-connect/certs";

        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Override default expiration check
        OAuth2TokenValidator<Jwt> noExpiryCheck = jwt -> OAuth2TokenValidatorResult.success();
        jwtDecoder.setJwtValidator(noExpiryCheck);

        return jwtDecoder;
    }
}
