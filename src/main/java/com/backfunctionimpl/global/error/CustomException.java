package com.backfunctionimpl.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public class CustomException extends RuntimeException{
    private final ErrorCode errorCode;
    private String message;


    public CustomException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public CustomException(ErrorCode errorCode) {
        this.errorCode = errorCode;

    }
}
