package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;


import com.ABIC.CustomerRequest.web.serviceManagment.model.TemplateField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateWithFieldsRequestDTO {
    private String EnglishName;
    private String ArabicName;
    private String EnglishDescription;
    private String ArabicDescription;
    private Long groupId;
    private List<TemplateField> fields;
}
