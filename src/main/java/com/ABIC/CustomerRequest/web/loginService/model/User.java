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

@Entry(base = "ou=users", objectClasses = { "inetOrgPerson", "top" })
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class User {

    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name = "uid")
    private String email;

    @Attribute(name = "cn")
    private String lastName;

    @Attribute(name = "sn")
    private String fullName;

    @Attribute(name = "employeeNumber")
    private String userNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Attribute(name = "userPassword")
    private String password;

    public User(String email,String userNumber, String fullName,String lastName, String password) {
        this.email = email;
        this.userNumber = userNumber;
        this.fullName = fullName;
        this.lastName = lastName;
        this.password = password;
    }
}
