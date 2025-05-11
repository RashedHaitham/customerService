package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldValueDTO {
    private Long fieldId;
    private String labelAr;
    private String labelEn;
    private String value;
    private List<String> extraDataEn;
    private List<String> extraDataAr;
}
