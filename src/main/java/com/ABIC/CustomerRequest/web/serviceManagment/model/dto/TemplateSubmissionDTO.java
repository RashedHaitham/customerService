package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class TemplateSubmissionDTO {
    private Long groupId;
    private List<FieldValueDTO> values;
}
