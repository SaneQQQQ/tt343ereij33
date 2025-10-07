package com.tt343ereij33.dto;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
    public static String buildResponse(HttpStatus httpStatus, String message, String path) {
        return """
           {
             "error": {
               "code": %d,
               "status": "%s",
               "message": "%s",
               "path": "%s"
             }
           }
           """.formatted(httpStatus.value(), httpStatus.getReasonPhrase(), message, path);
    }
}
