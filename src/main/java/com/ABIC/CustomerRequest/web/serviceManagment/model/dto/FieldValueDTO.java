package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldValueDTO {
    private Long fieldId;
    private String labelAr;
    private String labelEn;
    private String value;
}
