package com.ABIC.CustomerRequest.web.serviceManagment.repository;

import com.ABIC.CustomerRequest.web.serviceManagment.model.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateFieldRepository extends JpaRepository<TemplateField, Long> {
    List<TemplateField> findByGroupId(Long groupId);
    Optional<TemplateField> findById(Long id);
}