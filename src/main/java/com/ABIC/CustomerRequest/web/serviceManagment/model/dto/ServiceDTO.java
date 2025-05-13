package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import com.ABIC.CustomerRequest.web.serviceManagment.model.Template;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceDTO {
    @NotNull(message = "Status ID cannot be null")
    private Long statusId;

    @NotNull(message = "Service type ID cannot be null")
    private Long serviceTypeId;

    @NotBlank(message = "Arabic name cannot be empty")
    private String ArabicName;

    @NotBlank(message = "English name cannot be empty")
    private String EnglishName;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotBlank(message = "Arabic description cannot be empty")
    private String ArabicDescription;

    private String ArabicPlaceHolder;

    private Long templateId;

    @Min(value = 0, message = "SLA time cannot be negative")
    private int slaTime;
}
