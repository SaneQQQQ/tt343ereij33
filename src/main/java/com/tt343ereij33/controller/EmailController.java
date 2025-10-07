package com.tt343ereij33.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String verificationToken) {
        //TODO: Implement Email verification functionality
        return new ResponseEntity<>("Not implemented yet", HttpStatus.NOT_FOUND);
    }
}