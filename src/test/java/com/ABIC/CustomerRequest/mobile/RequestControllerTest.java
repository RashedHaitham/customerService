package com.ABIC.CustomerRequest.mobile;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.controller.RequestController;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.RequestWrapper;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.ValidateRequest;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.AddRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.service.RequestService;
import com.ABIC.CustomerRequest.util.PaginatedResponse;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.web.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.web.serviceManagment.model.Services;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.TemplateSubmissionDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.service.ServiceManagementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestControllerTest {

    @Mock
    private ServiceManagementService serviceManagementService;

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllRequestsWithoutStatus() {
        Page<Request> mockPage = new PageImpl<>(List.of(new Request()));
        when(requestService.getAllRequests(eq("1234"), any(Pageable.class)))
                .thenReturn(mockPage);

        ResponseEntity<Response<PaginatedResponse<Request>>> response = requestController.getAllRequests(
                "session", "1234", null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getData().getContent().isEmpty());
    }

    @Test
    void testGetAllRequestsWithStatus() {
        Request.Status status = Request.Status.PENDING;
        Page<Request> mockPage = new PageImpl<>(List.of(new Request()));
        when(requestService.getRequestsByStatus(eq(status), any(Pageable.class)))
                .thenReturn(mockPage);

        ResponseEntity<Response<PaginatedResponse<Request>>> response = requestController.getAllRequests(
                "session", "1234", status, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getData().getContent().isEmpty());
    }

    @Test
    void testCreateRequestWithValidChannel() {
        RequestWrapper wrapper = getSampleRequestWrapper("34");

        when(requestService.saveRequest(any(), any(), any(), any()))
                .thenReturn(new Request());

        ResponseEntity<Response<String>> response = requestController.createRequest(wrapper);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Request created successfully", response.getBody().getData());
    }


    @Test
    void testCreateRequestWithInvalidChannel() {
        RequestWrapper wrapper = getSampleRequestWrapper("99");

        ResponseEntity<Response<String>> response = requestController.createRequest(wrapper);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Invalid Channel-Id", response.getBody().getData());
    }


    @Test
    void testGetServiceTypes() {
        List<ServiceType> types = List.of(new ServiceType());
        when(requestService.getAllServiceTypes()).thenReturn(types);

        ResponseEntity<Map<String, Object>> response = requestController.getServiceTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(types, response.getBody().get("data"));
    }

    @Test
    void testGetAllServices() {
        Page<Services> mockPage = new PageImpl<>(List.of(new Services()));
        when(serviceManagementService.getServicesByStatus(eq("Active"), any(Pageable.class)))
                .thenReturn(mockPage);

        ResponseEntity<Response<PaginatedResponse<Services>>> response = requestController.getAllServices(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getData().getContent().isEmpty());
    }

    @Test
    void testSubmitForm() {
        TemplateSubmissionDTO dto = new TemplateSubmissionDTO();
        when(requestService.submitTemplateForm(dto,"TRS052029221407438700594511285f","1234")).thenReturn("SUB123");

        ResponseEntity<String> response = requestController.submitForm(dto,"TRS052029221407438700594511285f");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("SUB123"));
    }

    private RequestWrapper getSampleRequestWrapper(String channelId) {
        AddRequestDTO addRequestDTO = new AddRequestDTO(
                "الرجاء حذف المحفظة",
                "IT",
                "1234",
                "someone"
        );

        ValidateRequest validateRequest = new ValidateRequest(
                "TRS052029221407438700594511285f",
                "cv123",
                "152",
                "109491",
                channelId
        );

        RequestWrapper wrapper = new RequestWrapper();
        wrapper.setAddRequestDTO(addRequestDTO);
        wrapper.setValidateRequest(validateRequest);
        return wrapper;
    }
}
