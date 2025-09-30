package com.tt343ereij33.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResponse> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(
                new JsonResponse("INTERNAL SERVER ERROR",
                        LocalDateTime.now().toString()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<JsonResponse> usernameNotFoundExceptionHandler(Exception exception) {
        return new ResponseEntity<>(
                new JsonResponse("User Not Found",
                        LocalDateTime.now().toString()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<JsonResponse> badCredentialsExceptionHandler(Exception exception) {
        return new ResponseEntity<>(
                new JsonResponse("Incorrect username or password",
                        LocalDateTime.now().toString()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<JsonResponse> userCreationExceptionHandler(Exception exception) {
        return new ResponseEntity<>(
                new JsonResponse(exception.getMessage(),
                        LocalDateTime.now().toString()),
                HttpStatus.CONFLICT);
    }

    public record JsonResponse(String message, String timestamp) {}
}
