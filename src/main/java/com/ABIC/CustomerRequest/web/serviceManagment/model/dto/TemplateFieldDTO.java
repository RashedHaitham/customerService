package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import jakarta.validation.Valid;
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
@Valid
public class TemplateFieldDTO {
    private Long id;

    @NotBlank(message = "English label is required")
    private String labelEn;

    @NotBlank(message = "Arabic label is required")
    private String labelAr;

    @NotBlank(message = "Control type is required")
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
