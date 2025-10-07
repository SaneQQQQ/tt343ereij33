package com.tt343ereij33.service.impl;

import com.tt343ereij33.entity.OAuth2User;
import com.tt343ereij33.entity.Role;
import com.tt343ereij33.entity.User;
import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.repository.impl.OAuth2UserDAO;
import com.tt343ereij33.repository.impl.UserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

import static com.tt343ereij33.utils.SqlErrorMessageConstants.EMAIL_ALREADY_EXISTS;
import static com.tt343ereij33.utils.SqlErrorMessageConstants.USERNAME_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDAO userDAO;
    private final OAuth2UserDAO oAuth2UserDAO;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userDAO.readByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Username not found");
        }
    }

    public Optional<User> loadUserByEmail(String email) throws UsernameNotFoundException {
        try {
            return userDAO.readByEmail(email).or(Optional::empty);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public boolean registerUser(String username, String email, String password) throws UserCreationException {
        try {
            return userDAO.create(User.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .role(Role.ROLE_USER)
                    .build());
        } catch (SQLException e) {
            if (e.getMessage().contains(USERNAME_ALREADY_EXISTS)) {
                throw new UserCreationException("Username already exists");
            } else if (e.getMessage().contains(EMAIL_ALREADY_EXISTS)) {
                throw new UserCreationException("Email already exists");
            }
            throw new UserCreationException("Unknown error during registration occurred");
        }
    }

    public boolean registerOAuth2User(String username, String email, CommonOAuth2Provider provider, String providerUserId) throws UserCreationException {
        //TODO: Check if email exists in the db... if it is link user/pass + Oauth2
        try {
            boolean result = registerUser(username, email, null);
            if (result) {
                Optional<User> optionalUser = loadUserByEmail(email);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    return oAuth2UserDAO.create(OAuth2User.builder()
                            .provider(provider)
                            .providerUserId(providerUserId)
                            .user(user)
                            .build());
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains(USERNAME_ALREADY_EXISTS)) {
                throw new UserCreationException("Username already exists");
            } else if (e.getMessage().contains(EMAIL_ALREADY_EXISTS)) {
                throw new UserCreationException("Email already exists");
            }
            throw new UserCreationException("Unknown error during registration occurred");
        }
        return false;
    }
}
