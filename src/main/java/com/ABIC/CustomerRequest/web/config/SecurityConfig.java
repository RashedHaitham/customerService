package com.ABIC.CustomerRequest.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTFilter jwtFilter;

    @Value("${ldap.url}")
    private String ldapUrl;


    public SecurityConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**","/api/requests/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs/swagger-config").permitAll()
                        .requestMatchers("/api/services/**").authenticated()
                )
                .formLogin(AbstractAuthenticationFilterConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("jwtToken")
                );
        return http.build();
    }


    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }

    @Bean
    public LdapAuthenticationManager ldapAuthenticationManager(BindAuthenticator bindAuthenticator) {
        return new LdapAuthenticationManager(bindAuthenticator);
    }

    @Bean
    public BindAuthenticator bindAuthenticator(DefaultSpringSecurityContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserDnPatterns(new String[]{"uid={0},ou=users", "uid={0},ou=admins"});
        return authenticator;
    }

    @Bean
    public DefaultSpringSecurityContextSource contextSource() {
        return new DefaultSpringSecurityContextSource(ldapUrl);
    }



}
