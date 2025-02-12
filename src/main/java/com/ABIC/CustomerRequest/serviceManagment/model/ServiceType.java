package com.ABIC.CustomerRequest.serviceManagment.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_types")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeEn;
    private String typeAr;
}
