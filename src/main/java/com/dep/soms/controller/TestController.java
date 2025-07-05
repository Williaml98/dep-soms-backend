package com.dep.soms.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
public class TestController {

    @GetMapping("/test")
    public Map<String, Object> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Public endpoint is working!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
