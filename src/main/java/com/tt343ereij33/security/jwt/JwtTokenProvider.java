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
@PropertySource("classpath:application.properties")
public class JwtTokenProvider {
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    @Value("${jwt.validityInMilliseconds:3600000}")
    private long jwtValidityInMilliseconds;
    private SecretKey secretKey;

    @Autowired
    public JwtTokenProvider(UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        String username = user.getUsername();
        String authorities = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtValidityInMilliseconds);
        return Jwts.builder()
                .claims(Jwts.claims()
                        .subject(username)
                        .add("roles", authorities)
                        .build())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return bearerToken != null && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException
                 | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        String username = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        User user = userDetailsServiceImpl.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
    }
}
