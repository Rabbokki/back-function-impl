package com.backfunctionimpl.global.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ErrorInfo {
    private String errorMessage;
    private HttpStatus httpStatus;
}
