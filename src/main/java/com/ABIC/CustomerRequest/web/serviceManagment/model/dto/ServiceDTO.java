package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import com.ABIC.CustomerRequest.web.serviceManagment.model.Template;
import lombok.Data;

@Data
public class ServiceDTO {
    private Long statusId;
    private Long serviceTypeId;

    private String ArabicName;
    private String EnglishName;

    private String description;
    private String ArabicDescription;

    private String ArabicPlaceHolder;

    private Long templateId;

    private int slaTime;
}
