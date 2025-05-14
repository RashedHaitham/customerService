package com.ABIC.CustomerRequest.web.loginService.controller;

import com.ABIC.CustomerRequest.web.loginService.model.User;
import com.ABIC.CustomerRequest.web.loginService.model.dto.LoginRequest;
import com.ABIC.CustomerRequest.web.loginService.model.dto.LoginResponse;
import com.ABIC.CustomerRequest.web.loginService.model.dto.UserDTO;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.web.loginService.service.ServiceAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ServiceAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceAuthService serviceAuthService;

    @InjectMocks
    private ServiceAuthController serviceAuthController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(serviceAuthController).build();
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        User user = new User();
        user.setId(LdapNameBuilder.newInstance().build());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setLastName("User");
        user.setUserNumber("12345");

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(user);
        loginResponse.setToken("jwt-token-123");

        when(serviceAuthService.authenticateUser(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.lastName").value("User"))
                .andExpect(cookie().exists("jwtToken"))
                .andExpect(cookie().value("jwtToken", "jwt-token-123"))
                .andExpect(cookie().httpOnly("jwtToken", true))
                .andExpect(cookie().secure("jwtToken", true));

        verify(serviceAuthService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void testSignUp_Success() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setFullName("New User");
        userDTO.setLastName("User");
        userDTO.setUserNumber("12345");
        userDTO.setEmail("new@example.com");
        userDTO.setRole("USER");
        userDTO.setPassword("password123");

        doNothing().when(serviceAuthService).savePerson(any(UserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").value("Signup successful"));

        verify(serviceAuthService, times(1)).savePerson(any(UserDTO.class));
    }

    @Test
    void testSignUp_Failure() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setFullName("Existing User");
        userDTO.setLastName("User");
        userDTO.setUserNumber("67890");
        userDTO.setEmail("existing@example.com");
        userDTO.setRole("USER");
        userDTO.setPassword("password123");

        doThrow(new RuntimeException("Email already exists")).when(serviceAuthService).savePerson(any(UserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.data").value("Error: Email already exists"));

        verify(serviceAuthService, times(1)).savePerson(any(UserDTO.class));
    }

    @Test
    void testLogin_WithHttpServletResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        User user = new User();
        user.setId(LdapNameBuilder.newInstance().build());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setLastName("User");
        user.setUserNumber("12345");

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(user);
        loginResponse.setToken("jwt-token-123");

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        when(serviceAuthService.authenticateUser(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act
        var responseEntity = serviceAuthController.login(loginRequest, servletResponse);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Response<User> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(user, response.getData());

        Cookie[] cookies = servletResponse.getCookies();
        assertEquals(1, cookies.length);
        assertEquals("jwtToken", cookies[0].getName());
        assertEquals("jwt-token-123", cookies[0].getValue());
        assertTrue(cookies[0].isHttpOnly());
        assertTrue(cookies[0].getSecure());
        assertEquals(3600, cookies[0].getMaxAge());
        assertEquals("/", cookies[0].getPath());
    }
}
