package com.pranit.github.authorization.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RoleAlreadyAssignedException extends BaseException {
    public RoleAlreadyAssignedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
