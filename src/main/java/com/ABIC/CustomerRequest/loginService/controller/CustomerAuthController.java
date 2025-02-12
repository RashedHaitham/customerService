package com.ABIC.CustomerRequest.loginService.controller;

import com.ABIC.CustomerRequest.loginService.model.User;
import com.ABIC.CustomerRequest.loginService.model.dto.LoginRequest;
import com.ABIC.CustomerRequest.loginService.model.dto.LoginResponse;
import com.ABIC.CustomerRequest.loginService.model.dto.UserDTO;
import com.ABIC.CustomerRequest.loginService.service.CustomerAuthService;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    @Autowired
    public CustomerAuthController(CustomerAuthService customerAuthService1) {
        this.customerAuthService = customerAuthService1;
    }

    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping("/login")
    public ResponseEntity<Response<User>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = customerAuthService.authenticateUser(loginRequest);

        Cookie cookie = new Cookie("jwtToken", loginResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(3600);
        cookie.setPath("/");

        response.addCookie(cookie);

        Response<User> apiResponse = ResponseUtils.success(HttpStatus.OK.value(), loginResponse.getUser());
        return ResponseEntity.ok(apiResponse);
    }

    @ApiResponse(responseCode = "200", description = "Signup successful")
    @ApiResponse(responseCode = "400", description = "Signup failed")
    @PostMapping("/signup")
    public ResponseEntity<Response<String>> signUp(@RequestBody UserDTO user) {
        try {
            customerAuthService.savePerson(user);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Signup successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<String> errorResponse = ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

}
