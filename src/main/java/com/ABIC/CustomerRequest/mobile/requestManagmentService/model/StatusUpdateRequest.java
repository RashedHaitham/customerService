package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequest {
    @NotNull(message = "Status cannot be null")
    private Request.Status status;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;
}
