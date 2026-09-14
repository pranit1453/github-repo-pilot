package com.pranit.github.repo.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RepositoryNotFoundException extends BaseException {
    public RepositoryNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
