package com.ABIC.CustomerRequest.requestManagmentService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "requests")
@SequenceGenerator(name = "request_seq", sequenceName = "request_seq", allocationSize = 1)
public class Request {
    @Id
    private String requestNumber;
    private String description;
    private String requestedBy;
    private String customerNumber;
    private String statusUpdatedBy;
    @Enumerated(EnumType.STRING)
    private Status status;

    private String serviceType;

    private LocalDateTime time;
    private String slaTime;

    private String comment="";

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    @PrePersist
    private void generateRequestNumber() {
        if (this.requestNumber == null) {
            this.requestNumber = "REQ-" + getNextSequence();
        }
    }

    @Transient
    private synchronized String getNextSequence() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        EntityManager em = emf.createEntityManager();
        Query query = em.createNativeQuery("SELECT nextval('request_seq')");
        Integer nextVal = ((Number) query.getSingleResult()).intValue();
        em.close();
        emf.close();
        return String.valueOf(nextVal);
    }

}
