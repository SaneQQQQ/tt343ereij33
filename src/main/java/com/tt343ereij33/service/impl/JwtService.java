package com.tt343ereij33.service.impl;

import com.tt343ereij33.dto.JwtResponse;
import com.tt343ereij33.entity.UserEntity;
import com.tt343ereij33.entity.enums.Client;
import com.tt343ereij33.repository.JedisRepository;
import com.tt343ereij33.security.jwt.JwtTokenProvider;
import com.tt343ereij33.utils.RefreshTokenBody;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.RefreshFailedException;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JedisRepository jedisRepository;
    private JwtTokenProvider jwtTokenProvider;

    // TODO: Resolve circular dependency and remove setter
    @Autowired
    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public JwtResponse generateTokenPair(UserEntity user, String userAgent, Client client) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        jedisRepository.saveRefreshToken(refreshToken, jwtTokenProvider.getRefreshTokenValidity(),
                RefreshTokenBody.builder()
                        .userId(user.getId())
                        .oAuth2Provider(client)
                        .userAgent(userAgent)
                        .expiredAt(jwtTokenProvider.getRefreshTokenExpiration(refreshToken))
                        .issuedAt(jwtTokenProvider.getRefreshTokenIssuedAt(refreshToken))
                        .build());

        return JwtResponse.buildResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenValidity());
    }

    public JwtResponse regenerateTokenPair(UserEntity user, String refreshToken, String userAgent, Client client) throws RefreshFailedException {
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // TODO: Use single transaction here
        RefreshTokenBody refreshTokenBody = jedisRepository.getBodyByToken(refreshToken);

        if (refreshTokenBody == null || !refreshTokenBody.getUserAgent().equals(userAgent) || !refreshTokenBody.getOAuth2Provider().equals(client)) {
            throw new RefreshFailedException("Failing to refresh the token");
        }

        refreshTokenBody.setIssuedAt(jwtTokenProvider.getRefreshTokenIssuedAt(newRefreshToken));
        refreshTokenBody.setExpiredAt(jwtTokenProvider.getRefreshTokenExpiration(newRefreshToken));

        jedisRepository.saveRefreshToken(newRefreshToken, jwtTokenProvider.getRefreshTokenValidity(),
                refreshTokenBody);
        jedisRepository.deleteRefreshToken(refreshToken);

        return JwtResponse.buildResponse(newAccessToken, newRefreshToken, jwtTokenProvider.getAccessTokenValidity());
    }

    public void removeRefreshToken(String refreshToken) {
        jedisRepository.deleteRefreshToken(refreshToken);
    }
}
