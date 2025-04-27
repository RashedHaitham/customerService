package com.ABIC.CustomerRequest.web.serviceManagment.repository;

import com.ABIC.CustomerRequest.web.serviceManagment.model.Services;
import com.ABIC.CustomerRequest.web.serviceManagment.model.TemplateFieldValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateFieldValueRepository extends JpaRepository<TemplateFieldValue,Long> {
    Page<TemplateFieldValue> getAllByCustomerNumber(String customerNumber,
                                               Pageable pageable);

    void deleteByFieldId(Long id);

    List<TemplateFieldValue> findByCustomerNumber(String customerNumber);
}
