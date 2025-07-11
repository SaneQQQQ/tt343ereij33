package com.tt343ereij33.controller;

import com.tt343ereij33.dto.AuthenticationRequestDTO;
import com.tt343ereij33.dto.RegistrationRequestDTO;
import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.security.jwt.JwtTokenProvider;
import com.tt343ereij33.service.impl.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication API")
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenProvider jwtTokenProvider,
                                    UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authenticationRequestDTO.username(),
                authenticationRequestDTO.password());
        authenticationManager.authenticate(authenticationToken);
        String token = jwtTokenProvider
                .createToken(userDetailsService
                        .loadUserByUsername(authenticationRequestDTO.username()));
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody RegistrationRequestDTO registrationRequestDTO) {
        try {
            if (userDetailsService.register(
                    registrationRequestDTO.username(),
                    registrationRequestDTO.email(),
                    registrationRequestDTO.password())) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            return new ResponseEntity<>("Error creating user", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserCreationException e) {
            return new ResponseEntity<>("Error creating user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
