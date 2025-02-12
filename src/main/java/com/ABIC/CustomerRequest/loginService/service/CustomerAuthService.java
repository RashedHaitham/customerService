package com.ABIC.CustomerRequest.loginService.service;

import com.ABIC.CustomerRequest.config.LdapAuthenticationManager;
import com.ABIC.CustomerRequest.loginService.model.Admin;
import com.ABIC.CustomerRequest.loginService.model.User;
import com.ABIC.CustomerRequest.loginService.model.dto.LoginRequest;
import com.ABIC.CustomerRequest.loginService.model.dto.LoginResponse;
import com.ABIC.CustomerRequest.loginService.model.dto.UserDTO;
import com.ABIC.CustomerRequest.loginService.repository.AdminRepository;
import com.ABIC.CustomerRequest.loginService.repository.UserRepository;
import com.ABIC.CustomerRequest.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import javax.naming.Name;
import org.springframework.ldap.NameNotFoundException;

import java.util.Collection;

@Service
public class CustomerAuthService {
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final LdapAuthenticationManager ldapAuthenticationManager;
    private final LdapTemplate ldapTemplate;
    private final AdminRepository adminRepository;

    @Autowired
    public CustomerAuthService(UserRepository userRepository, JWTUtil jwtUtil, LdapAuthenticationManager ldapAuthenticationManager, LdapTemplate ldapTemplate, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.ldapAuthenticationManager = ldapAuthenticationManager;
        this.ldapTemplate = ldapTemplate;
        this.adminRepository = adminRepository;
    }

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        Authentication authentication = ldapAuthenticationManager.authenticate(authToken);

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        if (user == null) {
            Admin admin = adminRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            user = new User(admin.getEmail(),admin.getEmployeeNumber(), admin.getFullName(),admin.getLastName(), admin.getPassword());
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String token = jwtUtil.generateToken(user, authorities);

        return new LoginResponse(token, user);
    }


    public void savePerson(UserDTO user) {
        user.setRole(user.getRole().toUpperCase());
        user.setUserNumber(user.getUserNumber().toUpperCase());
        String userOu = user.getRole().equals("ADMIN") ? "admins" : "users";

        Name dn = LdapNameBuilder.newInstance()
                .add("ou", userOu)
                .add("uid", user.getEmail())
                .build();

        try {
            ldapTemplate.lookup(dn);
            throw new IllegalStateException("Entry already exists: " + dn.toString());
        } catch (NameNotFoundException e) {

            DirContextAdapter context = new DirContextAdapter(dn);
            context.setAttributeValues("objectClass", new String[]{"inetOrgPerson", "top"});
            context.setAttributeValue("uid", user.getEmail());
            context.setAttributeValue("cn", user.getFullName());
            context.setAttributeValue("sn", user.getLastName());
            context.setAttributeValue("employeeNumber", user.getUserNumber());
            context.setAttributeValue("userPassword", user.getPassword());

            ldapTemplate.bind(context);
        }
    }


}
