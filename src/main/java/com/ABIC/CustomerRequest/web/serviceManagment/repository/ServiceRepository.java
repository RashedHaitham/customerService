package com.ABIC.CustomerRequest.web.serviceManagment.repository;

import com.ABIC.CustomerRequest.web.serviceManagment.model.Services;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Services, Long> {
}