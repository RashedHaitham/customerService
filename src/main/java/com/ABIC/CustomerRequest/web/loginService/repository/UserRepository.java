package com.ABIC.CustomerRequest.web.loginService.repository;

import com.ABIC.CustomerRequest.web.loginService.model.User;
import org.springframework.data.ldap.repository.LdapRepository;

import java.util.Optional;

public interface UserRepository extends LdapRepository<User> {
    public Optional<User> findByEmail(String email);
}
