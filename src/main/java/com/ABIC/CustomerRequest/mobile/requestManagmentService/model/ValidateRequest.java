package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateRequest {
    @NotBlank(message = "Session ID cannot be empty")
    private String sessionId;

    @NotBlank(message = "Client version cannot be empty")
    private String clientVersion;

    @NotBlank(message = "Service ID cannot be empty")
    private String serviceId;

    @NotBlank(message = "User ID cannot be empty")
    private String userId;

    @NotBlank(message = "Channel ID cannot be empty")
    private String channelId;
}
