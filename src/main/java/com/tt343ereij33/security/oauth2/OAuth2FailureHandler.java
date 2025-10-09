package com.tt343ereij33.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // TODO: Implement correct OAuth2 failure handling
        Authentication authentication = exception.getAuthenticationRequest();
        String provider = null;
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            provider = oauthToken.getAuthorizedClientRegistrationId().toLowerCase();
        } else if (authentication instanceof OAuth2LoginAuthenticationToken oauthToken) {
            provider = oauthToken.getClientRegistration().getRegistrationId().toLowerCase();
        }
        response.sendRedirect("/oauth2/callback/" + provider + "?error=oauth_failed");
    }
}
