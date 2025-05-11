package com.ABIC.CustomerRequest.web.serviceManagment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponseDTO {
    private Long id;

    private String arabicName;
    private String englishName;
    private String description;
    private String arabicDescription;

    private Long templateId;
    private String groupId;
    private String templateNameEn;
    private String templateNameAr;

    private Long serviceTypeId;
    private String serviceTypeNameEn;
    private String serviceTypeNameAr;

    private Long statusId;
    private String statusAr;
    private String statusEn;


    private int slaTime;
}

