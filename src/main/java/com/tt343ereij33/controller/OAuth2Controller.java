package com.tt343ereij33.controller;

import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {
    private final UserDetailsServiceImpl userDetailsService;

    @GetMapping("/sign-in/google")
    public RedirectView signInGoogle() {
        RedirectView redirectView = new RedirectView("/oauth2/authorization/GOOGLE");
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

    @GetMapping("/sign-in/github")
    public RedirectView signInGithub() {
        RedirectView redirectView = new RedirectView("/oauth2/authorization/GITHUB");
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

    @ResponseBody
    @GetMapping("/callback/google")
    public ResponseEntity<?> callbackGoogle(@AuthenticationPrincipal OAuth2User principal,
                                            @RequestHeader("User-Agent") String userAgent)
            throws OAuth2AuthenticationException, UserCreationException {
        if (principal == null) {
            throw new OAuth2AuthenticationException("Google OAuth2 authentication failed. Unable to retrieve user information from Google");
        }
        return new ResponseEntity<>(userDetailsService.authGoogle(principal, userAgent), HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/callback/github")
    public ResponseEntity<?> callbackGithub(@AuthenticationPrincipal OAuth2User principal,
                                            @RequestHeader("User-Agent") String userAgent)
            throws OAuth2AuthenticationException, UserCreationException {
        if (principal == null) {
            throw new OAuth2AuthenticationException("Google OAuth2 authentication failed. Unable to retrieve user information from Google");
        }
        return new ResponseEntity<>(userDetailsService.authGitHub(principal, userAgent), HttpStatus.OK);
    }
}