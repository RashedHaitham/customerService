package com.ABIC.CustomerRequest.web.loginService.exception;

import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@Hidden
public class LoginExceptionHandler  {

    private static final Logger logger = LoggerFactory.getLogger(LoginExceptionHandler.class);

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity<Response<String>> handleNameNotFoundException(NameNotFoundException ex) {
        logger.error("LDAP entry not found: {}", ex.getMessage());
        Response<String> response = ResponseUtils.error(HttpStatus.NOT_FOUND.value(), "LDAP entry not found: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Response<String>> handleIllegalStateException(IllegalStateException ex) {
        logger.error("Operation failed: {}", ex.getMessage());
        Response<String> response = ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), "Operation failed: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleGeneralException(Exception ex) {
        logger.error("An error occurred in login service: ", ex);
        Response<String> response = ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
