package com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseDTO {
    private String requestNumber;
    private Long serviceId;
    private String requestedBy;
    private String customerNumber;
    private LocalDateTime time;
    private Integer slaTime;
    private String statusUpdatedBy;
    private String status;
}