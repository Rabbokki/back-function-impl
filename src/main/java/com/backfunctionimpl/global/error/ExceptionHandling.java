package com.backfunctionimpl.global.error;

import com.backfunctionimpl.global.dto.ResponseDto;
import io.jsonwebtoken.io.SerializationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
@Slf4j
public class ExceptionHandling {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getErrorCode().getMessage());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    // WebClientResponseException 처리
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ResponseDto<?>> handleWebClientResponseException(WebClientResponseException e) {
        log.error("WebClientResponseException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode())
                .body(ResponseDto.fail("Skyscanner_API_Error", "Skyscanner API 호출 실패: " + e.getMessage()));
    }

    // MethodArgumentNotValidException 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();
        log.error("MethodArgumentNotValidException: {}", errorMessage);
        return ResponseEntity.badRequest().body(errorMessage);
    }

    // 일반 Exception 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleGeneralException(Exception e) {
        log.error("General Exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.fail("Server_Error", "서버 오류: " + e.getMessage()));
    }
    @ExceptionHandler(SerializationException.class)
    public ResponseEntity<ResponseDto<?>> handleSerializationException(SerializationException e) {
        log.error("Redis serialization error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "Serialization failed: " + e.getMessage()));
    }


}