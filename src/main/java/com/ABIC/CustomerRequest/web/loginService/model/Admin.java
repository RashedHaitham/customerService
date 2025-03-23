package com.ABIC.CustomerRequest.web.loginService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;

@Entry(base = "ou=admins", objectClasses = { "inetOrgPerson", "top" })
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Admin {

    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name = "uid")
    private String email;

    @Attribute(name = "employeeNumber")
    private String employeeNumber;

    @Attribute(name = "cn")
    private String lastName;

    @Attribute(name = "sn")
    private String fullName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Attribute(name = "userPassword")
    private String password;

}
