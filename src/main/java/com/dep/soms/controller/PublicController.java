package com.dep.soms.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "API is running");
        return response;
    }

    @PostMapping("/echo")
    public Map<String, Object> echo(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", request);
        response.put("status", "success");
        response.put("message", "Request received successfully");
        return response;
    }
}