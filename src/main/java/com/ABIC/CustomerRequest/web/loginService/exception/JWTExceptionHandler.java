package com.ABIC.CustomerRequest.web.loginService.exception;

import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import io.jsonwebtoken.security.SignatureException;

@ControllerAdvice
@Hidden
public class JWTExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(JWTExceptionHandler.class);

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Response<String>> handleSignatureException(SignatureException ex) {
        logger.error("JWT signature validation failed: {}", ex.getMessage());
        Response<String> response = ResponseUtils.error(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Response<String>> handleExpiredJwtException(ExpiredJwtException ex) {
        logger.error("JWT token has expired: {}", ex.getMessage());
        Response<String> response = ResponseUtils.error(HttpStatus.UNAUTHORIZED.value(), "JWT token has expired: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Response<String>> handleMalformedJwtException(MalformedJwtException ex) {
        logger.error("Invalid JWT token: {}", ex.getMessage());
        Response<String> response = ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), "Invalid JWT token: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("JWT claims string is empty: {}", ex.getMessage());
        Response<String> response = ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), "JWT claims string is empty: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleGeneralException(Exception ex) {
        logger.error("Error processing JWT: ", ex);
        Response<String> response = ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error processing JWT: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
