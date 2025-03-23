package com.ABIC.CustomerRequest.web.loginService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String fullName;
    private String lastName;
    private String userNumber;
    private String email;
    private String role;
    private String password;
}
