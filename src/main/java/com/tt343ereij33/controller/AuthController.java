package com.tt343ereij33.controller;

import com.tt343ereij33.dto.AuthenticationRequest;
import com.tt343ereij33.dto.RegistrationRequest;
import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.service.impl.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.RefreshFailedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid RegistrationRequest registrationRequest)
            throws UserCreationException {
        userDetailsService.registerUser(registrationRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid AuthenticationRequest authenticationRequest,
                                    @RequestHeader("User-Agent") String userAgent) {
        return new ResponseEntity<>(userDetailsService.authLocal(authenticationRequest, userAgent), HttpStatus.OK);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(@RequestHeader("X-Refresh-Token") String refreshToken) {
        userDetailsService.signOut(refreshToken);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("X-Refresh-Token") String refreshToken,
                                     @RequestHeader("User-Agent") String userAgent)
            throws RefreshFailedException {
        return new ResponseEntity<>(userDetailsService.refresh(refreshToken, userAgent), HttpStatus.OK);
    }
}
