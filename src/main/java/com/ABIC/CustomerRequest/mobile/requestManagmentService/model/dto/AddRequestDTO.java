package com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRequestDTO {

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Service cannot be empty")
    private Long serviceId;

    @NotBlank(message = "Customer number cannot be empty")
    private String customerNumber;

    @NotBlank(message = "Requested by cannot be empty")
    private String requestedBy;

}
