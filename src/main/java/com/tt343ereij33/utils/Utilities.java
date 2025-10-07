package com.tt343ereij33.utils;

import jakarta.servlet.http.HttpServletRequest;

public class Utilities {
    private static String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
