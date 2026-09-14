package com.pranit.github.authentication.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class UserNotExistsException extends BaseException {
    public UserNotExistsException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
