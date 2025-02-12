package com.ABIC.CustomerRequest.serviceManagment.repository;

import com.ABIC.CustomerRequest.serviceManagment.model.ServiceStatus;
import com.ABIC.CustomerRequest.serviceManagment.model.Services;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceStatusRepository extends JpaRepository<ServiceStatus, Long> {
}