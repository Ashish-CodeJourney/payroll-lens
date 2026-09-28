package com.acme.payrolllens.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidInput(IllegalArgumentException exception) {
        return ResponseEntity.unprocessableEntity().body(new ApiError("VALIDATION_ERROR", exception.getMessage()));
    }
}
