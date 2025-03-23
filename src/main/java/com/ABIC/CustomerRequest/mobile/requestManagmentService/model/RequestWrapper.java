package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import lombok.Data;

@Data
public class RequestWrapper {
    private ValidateRequest validateRequest;
    private AddRequestDTO addRequestDTO;
}
