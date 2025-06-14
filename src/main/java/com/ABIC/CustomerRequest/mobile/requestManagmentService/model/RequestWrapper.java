package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.AddRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestWrapper {
    @NotNull(message = "Validation request cannot be null")
    private ValidateRequest validateRequest;

    @NotNull(message = "Request data cannot be null")
    @Valid
    private AddRequestDTO addRequestDTO;
}
