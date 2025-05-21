package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.ValidateRequest;
import lombok.Data;

import java.util.List;

@Data
public class TemplateSubmissionDTO {
    private ValidateRequest validateRequest;
    private Long groupId;
    private List<FieldValueDTO> values;
}
