package com.bank.web.app.api.gatway.ApiGatway.Security;
import com.bank.web.app.api.gatway.ApiGatway.Redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

//    @Autowired
//    private RedisService service;

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        System.err.println(">>> [DEBUG] JWT received");
        try{
        }catch (Exception e){
            System.err.println(e.getMessage());
        }

        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);

        System.err.println(">>> [DEBUG] Extracted Authorities: " + authorities);

        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return List.of();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
