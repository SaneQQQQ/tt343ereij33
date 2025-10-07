package com.tt343ereij33.controller;

import com.tt343ereij33.dto.JwtResponse;
import com.tt343ereij33.entity.User;
import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.service.impl.JwtService;
import com.tt343ereij33.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @GetMapping("/sign-in/google")
    public RedirectView initiateGoogleLogin() {
        try {
            RedirectView redirectView = new RedirectView("/oauth2/authorization/GOOGLE");
            redirectView.setExposeModelAttributes(false);
            return redirectView;
        } catch (Exception e) {
            // In case of error, redirect to home with error parameter
            RedirectView errorView = new RedirectView("/home?oauth2=error");
            errorView.setExposeModelAttributes(false);
            return errorView;
        }
    }

    @ResponseBody
    @GetMapping("/callback/google")
    public ResponseEntity<?> handleGoogleCallback(@AuthenticationPrincipal OAuth2User principal) throws OAuth2AuthenticationException, UserCreationException {
        if (principal == null) {
            throw new OAuth2AuthenticationException("OAuth2 authentication failed");
        }

        String email = principal.getAttribute("email");
        String username = principal.getAttribute("name");
        String providerUserId = principal.getAttribute("sub");

        Optional<User> optionalUser = userDetailsService.loadUserByEmail(email);
        if (optionalUser.isEmpty()) {
            userDetailsService.registerOAuth2User(username, email, CommonOAuth2Provider.GOOGLE, providerUserId);
        }

        User user = userDetailsService.loadUserByEmail(email).get();

        JwtResponse response = jwtService.generateTokenPair(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}