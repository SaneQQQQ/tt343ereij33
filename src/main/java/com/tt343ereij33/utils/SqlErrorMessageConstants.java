package com.tt343ereij33.utils;

public class SqlErrorMessageConstants {
    private SqlErrorMessageConstants() {}
    public static final String USERNAME_ALREADY_EXISTS = "ERROR: duplicate key value violates unique constraint \"users_username_key\"";
    public static final String EMAIL_ALREADY_EXISTS = "ERROR: duplicate key value violates unique constraint \"users_email_key\"";
}
