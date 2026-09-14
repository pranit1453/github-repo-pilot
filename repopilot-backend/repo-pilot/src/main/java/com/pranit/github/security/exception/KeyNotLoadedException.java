package com.pranit.github.security.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class KeyNotLoadedException extends BaseException {
    public KeyNotLoadedException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
