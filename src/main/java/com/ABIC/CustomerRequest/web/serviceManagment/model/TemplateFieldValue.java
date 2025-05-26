package com.ABIC.CustomerRequest.web.serviceManagment.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_field_values")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private TemplateField field;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "sessionId",nullable = false)
    private String sessionId;

    @Column(name = "customerNumber",nullable = false)
    private String customerNumber;
}
