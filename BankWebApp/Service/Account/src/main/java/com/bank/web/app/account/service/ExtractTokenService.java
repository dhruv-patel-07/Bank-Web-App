package com.bank.web.app.account.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExtractTokenService {

    public Map<String,Object> extractValue(String headerToken) throws ParseException {

        String token = headerToken.substring(7);
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String username = claims.getStringClaim("preferred_username");
        String email = claims.getStringClaim("email");
        String name = claims.getStringClaim("name");
        boolean emailVerified = claims.getBooleanClaim("email_verified");
        String UID = claims.getStringClaim("sub");
        Map<String,Object> map = new HashMap<>();
        map.put("preferred_username",username);
        map.put("email",email);
        map.put("verified",emailVerified);
        map.put("uid",UID);
        map.put("name",name);
        return map;
    }
}
