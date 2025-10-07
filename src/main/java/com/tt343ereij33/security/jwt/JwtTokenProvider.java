package com.tt343ereij33.security.jwt;

import com.tt343ereij33.entity.User;
import com.tt343ereij33.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class JwtTokenProvider {
    @Value("${jwt.token.validityInMilliseconds:3600000}")
    private long jwtAccessTokenValidity;
    @Value("${refresh.token.validityInMilliseconds:2592000000}")
    private long jwtRefreshTokenValidity;
    private SecretKey jwtAccessSecretKey;
    private SecretKey jwtRefreshSecretKey;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @PostConstruct
    public void init() {
        jwtAccessSecretKey = Keys.hmacShaKeyFor(System.getenv("JWT_ACCESS_SECRET").getBytes(StandardCharsets.UTF_8));
        jwtRefreshSecretKey = Keys.hmacShaKeyFor(System.getenv("JWT_REFRESH_SECRET").getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        String username = user.getUsername();
        String authorities = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtAccessTokenValidity);
        return Jwts.builder()
                .claims(Jwts.claims()
                        .subject(username)
                        .add("roles", authorities)
                        .build())
                .issuedAt(now)
                .expiration(validity)
                .signWith(jwtAccessSecretKey, Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(User user) {
        String username = user.getUsername();
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtRefreshTokenValidity);
        return Jwts.builder()
                .claims(Jwts.claims()
                        .subject(username)
                        .build())
                .issuedAt(now)
                .expiration(validity)
                .signWith(jwtRefreshSecretKey, Jwts.SIG.HS512)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return bearerToken != null && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : null;
    }

    public Authentication getAuthentication(String token) {
        User user = userDetailsServiceImpl.loadUserByUsername(getAccessTokenUsername(token));
        return new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, jwtAccessSecretKey);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, jwtRefreshSecretKey);
    }

    public String getAccessTokenUsername(String token) {
        return getUsername(token, jwtAccessSecretKey);
    }

    public String getRefreshTokenUsername(String token) {
        return getUsername(token, jwtRefreshSecretKey);
    }

    public long getAccessTokenValidity() {
        return jwtAccessTokenValidity / 1000;
    }

    public long getRefreshTokenValidity() {
        return jwtRefreshTokenValidity / 1000;
    }

    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException
                 | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String getUsername(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
