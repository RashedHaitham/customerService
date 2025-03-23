package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestStatusSummary {
    private String status;
    private String statusArabic;
    private long count;
}