package com.tt343ereij33.security.oauth2;

import com.tt343ereij33.utils.Utilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String provider = null;
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            provider = oauthToken.getAuthorizedClientRegistrationId().toLowerCase();
        } else if (authentication instanceof OAuth2LoginAuthenticationToken oauthToken) {
            provider = oauthToken.getClientRegistration().getRegistrationId().toLowerCase();
        }

        Utilities.setResponseHeaders(request, response);
        response.sendRedirect("/oauth2/callback/" + provider);
    }
}