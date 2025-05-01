package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateWithFieldsRequestDTO {
    private String englishName;
    private String arabicName;
    private String englishDescription;
    private String arabicDescription;

    private List<TemplateFieldDTO> fields;
}
