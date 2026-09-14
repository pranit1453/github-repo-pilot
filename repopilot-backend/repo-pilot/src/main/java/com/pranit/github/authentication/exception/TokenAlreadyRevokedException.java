package com.pranit.github.authentication.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TokenAlreadyRevokedException extends BaseException {
    public TokenAlreadyRevokedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
