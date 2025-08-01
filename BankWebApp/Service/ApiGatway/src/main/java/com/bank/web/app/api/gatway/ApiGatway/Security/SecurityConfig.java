package com.bank.web.app.api.gatway.ApiGatway.Security;

import com.bank.web.app.api.gatway.ApiGatway.Redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity){
        serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange->
                        exchange.pathMatchers("/eureka/**","/api/v1/auth/login","/api/v1/auth/register/**","/api/v1/auth/verify/**","/api/v1/auth/**","/api/v1/account/user/recurring-account-calculator","/api/v1/account/user/heartbeat","/api/v1/download/heartbeat","/api/v1/transaction/heartbeat")
//                        exchange.pathMatchers("/eureka/**","/api/v1/auth/**")
                                .permitAll()
                                .pathMatchers("/api/v1/transaction/get-list-balance","/api/v1/transaction/emi-deduct").hasRole("service")
                                .pathMatchers("/api/v1/account/user/pending-active-account","/api/v1/account/user/active-account/**","/api/v1/account/user/add-interst-rate","/api/v1/transaction/admin/freeze","/api/v1/transaction/employee/branch-report","/api/v1/account/user/add-interst-rate").hasRole("employee")
                                .pathMatchers("/api/v1/account/user/add-branch","/api/v1/transaction/admin/branch-report/**").hasRole("admin")
                                .anyExchange()
                                .authenticated()
                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtSpec ->
                                jwtSpec.jwtAuthenticationConverter(new KeycloakRoleConverter())
                        )
                );
        return serverHttpSecurity.build();
    }

}
