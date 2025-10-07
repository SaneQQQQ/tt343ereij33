package com.tt343ereij33.controller;

import com.tt343ereij33.dto.AuthenticationRequestDTO;
import com.tt343ereij33.dto.JwtResponse;
import com.tt343ereij33.dto.RegistrationRequestDTO;
import com.tt343ereij33.entity.User;
import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.security.jwt.JwtTokenProvider;
import com.tt343ereij33.service.impl.JwtService;
import com.tt343ereij33.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final JwtService jwtService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody RegistrationRequestDTO registrationRequestDTO) throws UserCreationException {
        if (userDetailsService.registerUser(
                registrationRequestDTO.username(),
                registrationRequestDTO.email(),
                registrationRequestDTO.password())) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        throw new UserCreationException("Unknown error during registration occurred");
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequestDTO.username(),
                        authenticationRequestDTO.password()));
        User user = userDetailsService.loadUserByUsername(authenticationRequestDTO.username());
        JwtResponse response = jwtService.generateTokenPair(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) throws RefreshFailedException {
        if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
            String username = jwtTokenProvider.getRefreshTokenUsername(refreshToken);
            User user = userDetailsService.loadUserByUsername(username);
            JwtResponse response = jwtService.regenerateTokenPair(user, refreshToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        throw new RefreshFailedException("Invalid or expired refresh token");
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(@RequestHeader("X-Refresh-Token") String refreshToken) {
        jwtService.removeRefreshToken(refreshToken);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
