package com.ABIC.CustomerRequest.web.serviceManagment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_fields")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String labelEn;

    @Column(nullable = false)
    private String labelAr;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_type", nullable = false)
    private ControlType controlType;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private boolean attachment;

    @Column(nullable = false)
    private int sorting;

    private String placeholderEn;

    private String placeholderAr;

    @Column(name = "extra_data_en", columnDefinition = "TEXT")
    private String extraDataEn;  // Can be JSON or comma-separated values

    @Column(name = "extra_data_ar", columnDefinition = "TEXT")
    private String extraDataAr;

    @Column(name = "group_id", nullable = false)
    @JsonIgnore
    private Long groupId;

    @Column(name = "hidden", nullable = false)
    @JsonIgnore
    private boolean hidden = false;

}
