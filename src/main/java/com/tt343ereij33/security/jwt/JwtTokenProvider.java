package com.tt343ereij33.security.jwt;

import com.tt343ereij33.entity.UserEntity;
import com.tt343ereij33.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserDetailsServiceImpl userDetailsServiceImpl;

    // TODO: Resolve circular dependency and remove setter
    @Autowired
    public void setUserDetailsServiceImpl(UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @PostConstruct
    public void init() {
        jwtAccessSecretKey = Keys.hmacShaKeyFor(System.getenv("JWT_ACCESS_SECRET").getBytes(StandardCharsets.UTF_8));
        jwtRefreshSecretKey = Keys.hmacShaKeyFor(System.getenv("JWT_REFRESH_SECRET").getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserEntity user) {
        return generateToken(user, jwtAccessSecretKey, jwtAccessTokenValidity);
    }

    public String generateRefreshToken(UserEntity user) {
        return generateToken(user, jwtRefreshSecretKey, jwtRefreshTokenValidity);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return bearerToken != null && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : null;
    }

    public Authentication getAuthentication(String token) {
        UserEntity user = userDetailsServiceImpl.loadUserByUsername(getAccessTokenUsername(token));
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

    public long getRefreshTokenExpiration(String refreshToken) {
        return Jwts.parser()
                .verifyWith(jwtRefreshSecretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getExpiration()
                .getTime();
    }

    public long getRefreshTokenIssuedAt(String refreshToken) {
        return Jwts.parser()
                .verifyWith(jwtRefreshSecretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getIssuedAt()
                .getTime();
    }

    private String generateToken(UserEntity user, SecretKey key, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .claims(generateClaims(user, key.equals(jwtAccessSecretKey)))
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    private Claims generateClaims(UserEntity user, boolean setRoles) {
        ClaimsBuilder claimsBuilder = Jwts.claims().subject(user.getUsername());
        if (setRoles){
            claimsBuilder.add("roles", getAuthorities(user));
        }
        return claimsBuilder.build();
    }

    private String getAuthorities(UserEntity user) {
        return user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
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
