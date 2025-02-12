package com.ABIC.CustomerRequest.loginService.model.dto;

import com.ABIC.CustomerRequest.loginService.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponse {
    String token;
    User user;
}
