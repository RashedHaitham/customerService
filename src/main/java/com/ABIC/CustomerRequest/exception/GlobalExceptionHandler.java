package com.ABIC.CustomerRequest.exception;

import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.error("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.error("Constraint violation: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), errors));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<String>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        logger.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseUtils.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<String>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        logger.error("Business exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleAllExceptions(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtils.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                        "An unexpected error occurred. Please try again later."));
    }
}