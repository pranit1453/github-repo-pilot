package com.pranit.github.repo.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RepositoryAlreadyExistsException extends BaseException {
    public RepositoryAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
