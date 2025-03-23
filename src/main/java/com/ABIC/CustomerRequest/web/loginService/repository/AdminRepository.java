package com.ABIC.CustomerRequest.web.loginService.repository;

import com.ABIC.CustomerRequest.web.loginService.model.Admin;
import org.springframework.data.ldap.repository.LdapRepository;

import java.util.Optional;

public interface AdminRepository extends LdapRepository<Admin> {
    public Optional<Admin> findByEmail(String email);
}
