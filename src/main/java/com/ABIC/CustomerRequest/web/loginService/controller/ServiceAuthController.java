package com.ABIC.CustomerRequest.web.loginService.controller;

import com.ABIC.CustomerRequest.exception.BusinessException;
import com.ABIC.CustomerRequest.exception.ResourceNotFoundException;
import com.ABIC.CustomerRequest.web.loginService.model.User;
import com.ABIC.CustomerRequest.web.loginService.model.dto.LoginRequest;
import com.ABIC.CustomerRequest.web.loginService.model.dto.LoginResponse;
import com.ABIC.CustomerRequest.web.loginService.model.dto.UserDTO;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import com.ABIC.CustomerRequest.web.loginService.service.ServiceAuthService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ServiceAuthController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthController.class);
    private final ServiceAuthService serviceAuthService;

    @Autowired
    public ServiceAuthController(ServiceAuthService serviceAuthService) {
        this.serviceAuthService = serviceAuthService;
    }

    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping("/login")
    public ResponseEntity<Response<User>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            logger.info("Processing login request for user: {}", loginRequest.getEmail());
            LoginResponse loginResponse = serviceAuthService.authenticateUser(loginRequest);

            Cookie cookie = new Cookie("jwtToken", loginResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(3600);
            cookie.setPath("/");

            response.addCookie(cookie);

            logger.info("Login successful for user: {}", loginRequest.getEmail());
            Response<User> apiResponse = ResponseUtils.success(HttpStatus.OK.value(), loginResponse.getUser());
            return ResponseEntity.ok(apiResponse);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the exception handlers handle these
            logger.error("Authentication failed for user {}: {}", loginRequest.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user {}: ", loginRequest.getEmail(), e);
            throw new BusinessException("Error during authentication: " + e.getMessage(), e);
        }
    }

    @ApiResponse(responseCode = "200", description = "Signup successful")
    @ApiResponse(responseCode = "400", description = "Signup failed")
    @PostMapping("/signup")
    public ResponseEntity<Response<String>> signUp(@RequestBody UserDTO user) {
        try {
            logger.info("Processing signup request for user: {}", user.getEmail());
            serviceAuthService.savePerson(user);
            logger.info("Signup successful for user: {}", user.getEmail());
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Signup successful");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the exception handlers handle these
            logger.error("Signup failed for user {}: {}", user.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during signup for user {}: ", user.getEmail(), e);
            Response<String> errorResponse = ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

}
