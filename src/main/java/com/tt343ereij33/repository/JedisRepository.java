package com.tt343ereij33.repository;

import com.tt343ereij33.utils.RefreshTokenBody;
import com.tt343ereij33.utils.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

@Repository
@RequiredArgsConstructor
public class JedisRepository {
    private final JedisPooled jedisPooled;
    private static final String PREFIX = "refresh:";

    public void saveRefreshToken(String token, long ttlMs, RefreshTokenBody body) {
        jedisPooled.setex(PREFIX + TokenHasher.hmacToken(token), ttlMs, RefreshTokenBody.toJson(body));
    }

    public RefreshTokenBody getBodyByToken(String token) {
        return RefreshTokenBody.fromJson(jedisPooled.get(PREFIX + TokenHasher.hmacToken(token)));
    }

    public void deleteRefreshToken(String token) {
        jedisPooled.del(PREFIX + TokenHasher.hmacToken(token));
    }
}