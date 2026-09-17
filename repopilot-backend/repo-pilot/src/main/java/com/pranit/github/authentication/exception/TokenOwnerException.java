package com.pranit.github.authentication.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TokenOwnerException extends BaseException {
    public TokenOwnerException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
