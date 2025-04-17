package com.ABIC.CustomerRequest.web.serviceManagment.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "templates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String EnglishName;

    @Column(nullable = false)
    private String ArabicName;

    @Column(columnDefinition = "TEXT")
    private String EnglishDescription;

    @Column(columnDefinition = "TEXT")
    private String ArabicDescription;

    @Column(name = "group_id", nullable = false, unique = true)
    private Long groupId;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

}
