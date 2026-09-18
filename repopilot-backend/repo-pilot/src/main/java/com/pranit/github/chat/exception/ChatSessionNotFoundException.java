package com.pranit.github.chat.exception;

import com.pranit.github.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ChatSessionNotFoundException extends BaseException {
    public ChatSessionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
