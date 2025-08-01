package com.bank.web.app.download.controller;

import com.bank.web.app.download.dto.ResponseDTO;
import com.bank.web.app.download.redis.RedisService;
import com.bank.web.app.download.service.DownloadService;
import com.bank.web.app.download.service.ExtractTokenService;
import com.bank.web.app.download.service.RatelimitService;
import com.netflix.discovery.converters.Auto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/download/")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private RatelimitService ratelimitService;

    @Autowired
    private RedisService redisService;




    @GetMapping("download-transaction-report")
    @CircuitBreaker(name = "downloadService",fallbackMethod = "downloadFallBack")
    @RateLimiter(name = "downloadRateLimiter",fallbackMethod = "downloadRateLimitFallBack")
    @Bulkhead(name = "downloadBulkheadService", fallbackMethod = "downloadBulkheadFallback")
    public ResponseEntity<?> downloadStatement(@RequestHeader("Authorization") String authHeader, @RequestParam(required = true) Long account, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String month, @RequestParam(required = true) String download) throws ParseException {
        if(!ratelimitService.isAllowedUser(authHeader)){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ResponseDTO("429","Daily limit exceeded",null));
        }

        Map<String, String> response = new HashMap<>();
        if (download.equalsIgnoreCase("no")) {
            downloadService.SendPdfData(authHeader, account, startDate, endDate, month, download);
            response.put("message", "Email sent successfully");
            response.put("status", "success");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } else if (download.equalsIgnoreCase("yes")) {
            ByteArrayInputStream pdf = downloadService.SendPdfData(authHeader, account, startDate, endDate, month, download);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Disposition", "inline;file=statement.pdf");
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdf));
        } else {
            response.put("message", "ensure that the correct statement generation option is selected !!!!");
            response.put("status", "failed");

            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    public ResponseEntity<?> downloadFallBack(@RequestHeader("Authorization") String authHeader, @RequestParam(required = true) Long account, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String month, @RequestParam(required = true) String download,Throwable ex) throws ParseException {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ResponseDTO("503", "Service Unavailable", ex.getMessage()));
    }

    public ResponseEntity<?> downloadRateLimiter(@RequestHeader("Authorization") String authHeader, @RequestParam(required = true) Long account, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String month, @RequestParam(required = true) String download,Throwable ex) throws ParseException {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ResponseDTO("429", "Service Unavailable to many requests", ex.getMessage()));
    }

    public ResponseEntity<?> downloadBulkheadFallback(@RequestHeader("Authorization") String authHeader, @RequestParam(required = true) Long account, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String month, @RequestParam(required = true) String download,Throwable ex) throws ParseException {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ResponseDTO("fallback", "Too many concurrent requests. Please try again later.", ex.getMessage()));
    }

    @GetMapping("heartbeat")
    public ResponseEntity<String> heartbeat() {
        log.info("Download service");
        return ResponseEntity.ok("ALIVE");
    }

    }
