package com.tt343ereij33.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt343ereij33.entity.enums.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenBody {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("oauth2_provider")
    private Client oAuth2Provider;
    @JsonProperty("user_agent")
    private String userAgent;
    @JsonProperty("issued_at")
    private Long issuedAt;
    @JsonProperty("expired_at")
    private Long expiredAt;

    public static String toJson(RefreshTokenBody body) {
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize RefreshTokenValue", e);
        }
    }

    public static RefreshTokenBody fromJson(String json) {
        if (json == null)
            return null;
        try {
            return OBJECT_MAPPER.readValue(json, RefreshTokenBody.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize RefreshTokenValue", e);
        }
    }
}
