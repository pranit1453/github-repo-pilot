package com.pranit.github.authorization.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends BaseException {
    public RoleNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
