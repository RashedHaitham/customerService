package com.ABIC.CustomerRequest.mobile.requestManagmentService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "requests")
public class Request {
    @Id
    private String requestNumber;

    @Column(nullable = false, updatable = false, unique = true)
    @JsonIgnore
    private Long sequenceId;

    @PrePersist
    public void generateId() {
        if (this.requestNumber == null) {  // Ensure only new records generate an ID
            this.requestNumber = "REQ-" + String.format("%06d", this.sequenceId);
        }
    }

    private String description;
    private String requestedBy;
    private String customerNumber;
    private String statusUpdatedBy;
    private String serviceType;

    @JsonIgnore
    private String sessionId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime time;
    private String slaTime;

    private String comment="";

    @JsonIgnore
    private String clientVersion;

    @JsonIgnore
    private String channelId;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }



}
