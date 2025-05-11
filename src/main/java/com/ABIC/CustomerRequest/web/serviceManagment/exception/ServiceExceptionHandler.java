package com.ABIC.CustomerRequest.web.serviceManagment.exception;

import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ServiceExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public Response<String> handleRuntimeException(RuntimeException ex) {
        return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }
}
