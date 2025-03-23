package com.ABIC.CustomerRequest.web.serviceManagment.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String statusEn;
    private String statusAr;
}
