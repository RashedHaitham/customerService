package com.ABIC.CustomerRequest.requestManagmentService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {
    private String description;
    private String requestedBy;
    private String customerNumber;
    private String statusUpdatedBy;
    private String status;
    private String serviceType;
    private String slaTime;
    private String comment;
}