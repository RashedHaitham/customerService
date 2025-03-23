package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateRequest {
    private String sessionId;
    private String clientVersion;
    private String serviceId;
    private String userId;
    private String channelId;
}
