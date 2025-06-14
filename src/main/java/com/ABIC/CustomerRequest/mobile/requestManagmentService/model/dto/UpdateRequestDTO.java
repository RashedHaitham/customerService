package com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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

    @Size(max = 100, message = "Requested by must not exceed 100 characters")
    private String requestedBy;

    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "Customer number must be alphanumeric")
    private String customerNumber;

    @Size(max = 100, message = "Status updated by must not exceed 100 characters")
    private String statusUpdatedBy;

    @Pattern(regexp = "^(PENDING|APPROVED|REJECTED)$", message = "Status must be one of: PENDING, APPROVED, REJECTED")
    private String status;

    private Long serviceId;

    @Min(value = 0, message = "SLA time cannot be negative")
    private int slaTime;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

}
