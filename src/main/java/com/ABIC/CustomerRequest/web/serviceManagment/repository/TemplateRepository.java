package com.ABIC.CustomerRequest.web.serviceManagment.repository;

import com.ABIC.CustomerRequest.web.serviceManagment.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template,Long> {

    Optional<Template> findByGroupId(String groupId);

    List<Template> findAllByHiddenIsFalse();
}
