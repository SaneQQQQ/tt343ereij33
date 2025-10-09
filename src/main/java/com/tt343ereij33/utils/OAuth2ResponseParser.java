package com.tt343ereij33.utils;

import com.tt343ereij33.entity.UserEntity;
import com.tt343ereij33.entity.enums.Role;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2ResponseParser {
    public static UserEntity getUserFromGoogleOAuth2Response(OAuth2User principal) throws OAuth2AuthenticationException {
        String email = principal.getAttribute("email");
        String username = Utilities.getUsernameFromEmail(email);
        String firstName = principal.getAttribute("given_name");
        String lastName = principal.getAttribute("family_name");
        return UserEntity.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(Role.ROLE_USER)
                .build();
    }

    public static UserEntity getUserFromGitHubOAuth2Response(OAuth2User principal) throws OAuth2AuthenticationException {
        String email = principal.getAttribute("email");
        String username = principal.getAttribute("login");
        String fullName = principal.getAttribute("name");
        String firstName = null;
        String lastName = null;
        if (fullName != null) {
            String[] split = fullName.split(" ", 2);
            firstName = split[0];
            lastName = split[1];
        }
        return UserEntity.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(Role.ROLE_USER)
                .build();
    }
}
