package com.tt343ereij33.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

public class Utilities {
    public static void setResponseHeaders(HttpServletRequest request, HttpServletResponse response) {
        Map<String,String> headers = new HashMap<>();
        headers.put("User-Agent", request.getHeader("User-Agent"));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.setHeader(header.getKey(), header.getValue());
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    public static String getUsernameFromEmail(String email) {
        return email.split("@")[0];
    }
}
