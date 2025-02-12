package com.ABIC.CustomerRequest.serviceManagment.model;

import lombok.Data;

@Data
public class ServiceDTO {
    private Long statusId;
    private Long serviceTypeId;

    private String ArabicName;
    private String EnglishName;

    private String description;
    private String ArabicDescription;

    private String placeHolder;
    private String ArabicPlaceHolder;

    private int slaTime;
}
