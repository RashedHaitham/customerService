package com.ABIC.CustomerRequest.web.serviceManagment.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "control_type_lookup")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControlTypeLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "label_en", nullable = false)
    private String labelEn;

    @Column(name = "label_ar", nullable = false)
    private String labelAr;
}
