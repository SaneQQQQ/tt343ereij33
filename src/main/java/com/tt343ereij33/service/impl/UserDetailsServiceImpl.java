package com.tt343ereij33.service.impl;

import com.tt343ereij33.dto.AuthenticationRequest;
import com.tt343ereij33.dto.JwtResponse;
import com.tt343ereij33.dto.RegistrationRequest;
import com.tt343ereij33.entity.OAuth2UserEntity;
import com.tt343ereij33.entity.UserEntity;
import com.tt343ereij33.entity.enums.Client;
import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.repository.impl.OAuth2UserDAO;
import com.tt343ereij33.repository.impl.UserDAO;
import com.tt343ereij33.security.jwt.JwtTokenProvider;
import com.tt343ereij33.utils.OAuth2ResponseParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.security.auth.RefreshFailedException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static com.tt343ereij33.utils.SqlErrorMessageConstants.EMAIL_ALREADY_EXISTS;
import static com.tt343ereij33.utils.SqlErrorMessageConstants.USERNAME_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDAO userDAO;
    private final OAuth2UserDAO oAuth2UserDAO;
    private final JwtService jwtService;
    private final JwtTokenProvider jwtTokenProvider;
    private AuthenticationManager authenticationManager;

    // TODO: Resolve circular dependency and remove setter
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userDAO.readByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Username not found");
        }
    }

    public Optional<UserEntity> loadUserByEmail(String email) throws UsernameNotFoundException {
        try {
            return userDAO.readByEmail(email).or(Optional::empty);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public boolean registerUser(RegistrationRequest registrationRequest) throws UserCreationException {
        try {
            return userDAO.create(RegistrationRequest.toUserEntity(registrationRequest));
        } catch (SQLException e) {
            if (e.getMessage().contains(USERNAME_ALREADY_EXISTS)) {
                throw new UserCreationException("Username already exists");
            } else if (e.getMessage().contains(EMAIL_ALREADY_EXISTS)) {
                throw new UserCreationException("Email already exists");
            }
            throw new UserCreationException("Unknown error during registration occurred");
        }
    }

    public boolean registerUser(UserEntity user) throws UserCreationException {
        try {
            return userDAO.create(user);
        } catch (SQLException e) {
            if (e.getMessage().contains(USERNAME_ALREADY_EXISTS)) {
                return false;
            } else if (e.getMessage().contains(EMAIL_ALREADY_EXISTS)) {
                throw new UserCreationException("Email already exists");
            }
            throw new UserCreationException("Unknown error during registration occurred");
        }
    }

    public boolean registerOAuth2User(UserEntity user, CommonOAuth2Provider provider, String providerUserId) throws UserCreationException {
        UserEntity createdUser;
        if (user.getEmail() == null || !userDAO.existsByEmail(user.getEmail())) {
            user.setUsername(generateAvailableUsername(user.getUsername()));
            registerUser(user);
            createdUser = loadUserByUsername(user.getUsername());
        } else {
            createdUser = loadUserByEmail(user.getEmail()).get();
        }
        return oAuth2UserDAO.create(OAuth2UserEntity.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .user(createdUser)
                .build());
    }

    public JwtResponse authGoogle(OAuth2User principal, String userAgent) throws UserCreationException {
        UserEntity user = OAuth2ResponseParser.getUserFromGoogleOAuth2Response(principal);
        String providerUserId = principal.getAttribute("sub"); // sub -> 100052535415681348503

        if (!oAuth2UserDAO.existsByProviderId(CommonOAuth2Provider.GOOGLE, providerUserId)) {
            registerOAuth2User(user, CommonOAuth2Provider.GOOGLE, providerUserId);
        }

        UserEntity readUser = loadUserByUsername(user.getUsername());

        return jwtService.generateTokenPair(readUser, userAgent, Client.GOOGLE);
    }

    public JwtResponse authGitHub(OAuth2User principal, String userAgent) throws UserCreationException, UsernameNotFoundException {
        UserEntity user = OAuth2ResponseParser.getUserFromGitHubOAuth2Response(principal);
        String providerUserId = principal.getAttribute("id").toString(); // id -> 79334057

        if (!oAuth2UserDAO.existsByProviderId(CommonOAuth2Provider.GITHUB, providerUserId)) {
            registerOAuth2User(user, CommonOAuth2Provider.GITHUB, providerUserId);
        }

        UserEntity readUser = loadUserByUsername(user.getUsername());

        return jwtService.generateTokenPair(readUser, userAgent,  Client.GITHUB);
    }

    public JwtResponse authLocal(AuthenticationRequest authenticationRequest, String userAgent) {
        // TODO: Check if I still need this authenticationManager.authenticate()
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.username(),
                        authenticationRequest.password()));
        UserEntity user = loadUserByUsername(authenticationRequest.username());
        return jwtService.generateTokenPair(user, userAgent, Client.LOCAL);
    }

    public JwtResponse refresh(String refreshToken, String userAgent) throws RefreshFailedException {
        if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
            String username = jwtTokenProvider.getRefreshTokenUsername(refreshToken);
            UserEntity user = loadUserByUsername(username);
            return jwtService.regenerateTokenPair(user, refreshToken, userAgent, Client.LOCAL);
        }
        throw new RefreshFailedException("Invalid or expired refresh token");

    }

    public void signOut(String refreshToken) {
        jwtService.removeRefreshToken(refreshToken);
    }

    public String generateAvailableUsername(String username) {
        String result = username;
        int attempt = 0;
        while (userDAO.existsByUsername(result) && attempt < 10) {
            result = "user_" + UUID.randomUUID().toString().substring(0, 12);
            attempt++;
        }
        return result;
    }
}
