package com.tt343ereij33.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JwtResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn
) {
    public static JwtResponse buildResponse(String accessToken, String refreshToken, long expiresIn) {
        return new JwtResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}