package com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRequestDTO {

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String requestedBy;

    private String customerNumber;

    private String statusUpdatedBy;

    private String status;

    private Long serviceId;

    private int slaTime;

    private String comment;

}
