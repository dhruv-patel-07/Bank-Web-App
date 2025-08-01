package com.bank.web.app.download.service;

import com.bank.web.app.download.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class RatelimitService {

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private RedisService rateLimiter;

    public boolean isAllowedUser(String Token) throws ParseException {
        Map<String,Object> map = extractTokenService.extractValue(Token);
        String uid = map.get("uid").toString();
        return rateLimiter.isAllowed(uid);
//        return true;

    }
}
