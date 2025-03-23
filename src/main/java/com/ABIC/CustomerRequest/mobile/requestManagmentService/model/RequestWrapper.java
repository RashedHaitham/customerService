package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.AddRequestDTO;
import lombok.Data;

@Data
public class RequestWrapper {
    private ValidateRequest validateRequest;
    private AddRequestDTO addRequestDTO;
}
