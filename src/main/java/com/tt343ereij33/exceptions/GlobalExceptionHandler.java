package com.tt343ereij33.exceptions;

import com.tt343ereij33.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.RefreshFailedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> internalServerErrorHandler(HttpServletRequest request) {
        return new ResponseEntity<>(
                ErrorResponse.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error", request.getServletPath()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> notFoundHandler(Exception exception, HttpServletRequest request) {
        return new ResponseEntity<>(
                ErrorResponse.buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getServletPath()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({BadCredentialsException.class, RefreshFailedException.class, OAuth2AuthenticationException.class})
    public ResponseEntity<String> unauthorizedHandler(Exception exception, HttpServletRequest request) {
        return new ResponseEntity<>(
                ErrorResponse.buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getServletPath()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<String> conflictHandler(Exception exception, HttpServletRequest request) {
        return new ResponseEntity<>(
                ErrorResponse.buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getServletPath()),
                HttpStatus.CONFLICT);
    }
}
