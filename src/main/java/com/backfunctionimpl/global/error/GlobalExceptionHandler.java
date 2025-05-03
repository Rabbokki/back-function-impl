package com.backfunctionimpl.global.error;

import com.backfunctionimpl.global.dto.ResponseDto;
import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDto<String>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .badRequest()
                .body(ResponseDto.fail(ErrorCode.INVALID_MESSAGE.getCode(), "Invalid or malformed JSON"));
    }

}
