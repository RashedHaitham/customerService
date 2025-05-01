package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldDTO {

    @NotBlank
    private String labelEn;

    @NotBlank
    private String labelAr;

    @NotNull
    private String controlType;

    private boolean required;
    private boolean attachment;

    @Min(0)
    private int sorting;

    private String placeholderEn;
    private String placeholderAr;

    private List<String> extraDataEn;
    private List<String> extraDataAr;
}
