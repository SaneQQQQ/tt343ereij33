package com.tt343ereij33.service.impl;

import com.tt343ereij33.dto.JwtResponse;
import com.tt343ereij33.entity.User;
import com.tt343ereij33.security.jwt.JwtTokenProvider;
import com.tt343ereij33.repository.JedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JedisRepository jedisRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtResponse generateTokenPair(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        jedisRepository.storeRefreshToken(refreshToken, user.getId(), jwtTokenProvider.getRefreshTokenValidity());

        return JwtResponse.buildResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenValidity());
    }

    public JwtResponse regenerateTokenPair(User user, String refreshToken) {
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        Long userIdByToken = jedisRepository.getUserIdByToken(refreshToken);
        jedisRepository.storeRefreshToken(newRefreshToken, userIdByToken, jwtTokenProvider.getRefreshTokenValidity());
        jedisRepository.deleteRefreshToken(refreshToken);

        return JwtResponse.buildResponse(newAccessToken, newRefreshToken, jwtTokenProvider.getAccessTokenValidity());
    }

    public void removeRefreshToken(String refreshToken) {
        jedisRepository.deleteRefreshToken(refreshToken);
    }

}
