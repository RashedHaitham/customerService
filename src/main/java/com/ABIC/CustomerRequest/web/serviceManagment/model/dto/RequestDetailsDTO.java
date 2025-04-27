package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDetailsDTO {
    private String requestNumber;
    private String description;
    private String requestedBy;
    private String customerNumber;
    private String statusUpdatedBy;
    private String serviceType;
    private String status;
    private LocalDateTime time;
    private String slaTime;
    private String comment;
    private List<FieldValueDTO> fields;
}
