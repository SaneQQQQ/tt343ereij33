package com.tt343ereij33.repository;

import com.tt343ereij33.utils.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

@Repository
@RequiredArgsConstructor
public class JedisRepository {
    private final JedisPooled jedisPooled;
    private static final String PREFIX = "refresh:";

    //TODO: store in value: {userId, issuedAt, expiresAt, ip (use Utilities method), userAgent (request.getHeader("User-Agent"))}
    public void storeRefreshToken(String token, Long userId, long ttlMs) {
        jedisPooled.setex(PREFIX + TokenHasher.hmacToken(token), ttlMs, userId.toString());
    }

    public Long getUserIdByToken(String token) {
        return Long.valueOf(jedisPooled.get(PREFIX + TokenHasher.hmacToken(token)));
    }

    public void deleteRefreshToken(String token) {
        jedisPooled.del(PREFIX + TokenHasher.hmacToken(token));
    }
}
