package com.tt343ereij33.exceptions;

public class UnknownOAuth2ClientException extends Exception {
    public UnknownOAuth2ClientException(String message) {
        super(message);
    }

    public UnknownOAuth2ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownOAuth2ClientException(Throwable cause) {
        super(cause);
    }
}
