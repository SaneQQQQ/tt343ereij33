package com.tt343ereij33.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {
    @GetMapping
    public ResponseEntity<?> getHome() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> response = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("message", "Welcome to the protected home resource! User: " + authentication.getName());
        } else {
            response.put("message", "Welcome to the home resource!");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

