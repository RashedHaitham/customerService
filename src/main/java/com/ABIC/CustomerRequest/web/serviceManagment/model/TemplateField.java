package com.ABIC.CustomerRequest.web.serviceManagment.model;

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
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_type", nullable = false)
    private ControlType controlType;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int sorting;

    private String placeholder;

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;  // Can be JSON or comma-separated values

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    private enum ControlType {
        TEXTBOX,
        RADIOBUTTON,
        DROPDOWN,
        CHECKBOX,
        DATEPICKER,
        TEXTAREA,
        NUMBER
    }

}
