package com.ABIC.CustomerRequest.serviceManagment.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "services")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Services {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ArabicName;
    private String EnglishName;

    private String description;
    private String ArabicDescription;

    private String placeHolder;
    private String ArabicPlaceHolder;

    @ManyToOne
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private ServiceStatus status;

    private int slaTime;
}
