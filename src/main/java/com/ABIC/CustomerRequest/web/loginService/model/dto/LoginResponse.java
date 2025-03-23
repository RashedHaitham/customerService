package com.ABIC.CustomerRequest.web.loginService.model.dto;

import com.ABIC.CustomerRequest.web.loginService.model.User;
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
