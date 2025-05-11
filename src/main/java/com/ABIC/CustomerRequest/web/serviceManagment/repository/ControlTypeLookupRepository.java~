package com.ABIC.CustomerRequest.web.serviceManagment.repository;

import com.ABIC.CustomerRequest.web.serviceManagment.model.ControlTypeLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlTypeLookupRepository extends JpaRepository<ControlTypeLookup, Long> {
    Optional<Object> findByCode(String controlTypeCode);
}
