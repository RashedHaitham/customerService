package com.ABIC.CustomerRequest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.authentication.LdapAuthenticator;

import java.util.ArrayList;
import java.util.List;

public class LdapAuthenticationManager implements AuthenticationManager {

    private final LdapAuthenticator ldapAuthenticator;

    public LdapAuthenticationManager(LdapAuthenticator ldapAuthenticator) {
        this.ldapAuthenticator = ldapAuthenticator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();


        // Authenticate the user with LDAP
        DirContextOperations context = ldapAuthenticator.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        String dn = context.getNameInNamespace();

        String role;
        if (dn.contains("ou=admins")) {
            role = "ADMIN";  // If the user is in ou=admins
        } else if (dn.contains("ou=users")) {
            role = "USER";   // If the user is in ou=users
        } else {
            throw new BadCredentialsException("Unknown OU: Unable to determine role");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));


        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
