package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequest {
    private Request.Status status;
    private String comment;
}