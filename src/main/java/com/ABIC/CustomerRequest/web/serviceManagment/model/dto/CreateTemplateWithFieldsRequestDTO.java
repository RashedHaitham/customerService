package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateWithFieldsRequestDTO {
    @NotBlank(message = "English name cannot be empty")
    @Size(max = 255, message = "English name must not exceed 255 characters")
    private String englishName;

    @NotBlank(message = "Arabic name cannot be empty")
    @Size(max = 255, message = "Arabic name must not exceed 255 characters")
    private String arabicName;

    @Size(max = 1000, message = "English description must not exceed 1000 characters")
    private String englishDescription;

    @Size(max = 1000, message = "Arabic description must not exceed 1000 characters")
    private String arabicDescription;

    @NotEmpty(message = "Fields list cannot be empty")
    @Valid
    private List<@Valid TemplateFieldDTO> fields;
}
