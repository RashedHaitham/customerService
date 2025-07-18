package com.ABIC.CustomerRequest.web;

import com.ABIC.CustomerRequest.exception.BusinessException;
import com.ABIC.CustomerRequest.exception.ResourceNotFoundException;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.RequestStatusSummary;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.StatusUpdateRequest;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.RequestResponseDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.UpdateRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.service.RequestService;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.web.config.JWTFilter;
import com.ABIC.CustomerRequest.web.config.SecurityConfig;
import com.ABIC.CustomerRequest.web.serviceManagment.controller.ServiceManagementController;
import com.ABIC.CustomerRequest.web.serviceManagment.model.*;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.CreateTemplateWithFieldsRequestDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.RequestDetailsDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.ServiceDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.ServiceResponseDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.TemplateFieldDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.service.ServiceManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ServiceManagementController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JWTFilter.class,
                        SecurityConfig.class
                })
        })
@AutoConfigureMockMvc(addFilters = false)

public class ServiceManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceManagementService serviceManagementService;


    @MockBean
    private RequestService requestService;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddService() throws Exception {
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setEnglishName("Test Service");
        serviceDTO.setArabicName("خدمة تجريبية");
        serviceDTO.setDescription("Service Description");
        serviceDTO.setArabicDescription("وصف الخدمة");
        serviceDTO.setSlaTime(5);
        serviceDTO.setStatusId(1L);
        serviceDTO.setServiceTypeId(1L);

        mockMvc.perform(post("/api/services/add-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").value("Service added successfully"));
    }

    @Test
    void testGetRequestByNumberFound() throws Exception {
        // Given
        RequestDetailsDTO dto = new RequestDetailsDTO();
        dto.setRequestNumber("REQ-100000");
        dto.setDescription("Sample description");

        when(serviceManagementService.getRequestDetails("REQ-100000")).thenReturn(Optional.of(dto));

        // When & Then
        mockMvc.perform(get("/api/services/requests/REQ-100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.requestNumber").value("REQ-100000"))
                .andExpect(jsonPath("$.data.description").value("Sample description"));
    }

    @Test
    void testGetRequestByNumberNotFound() throws Exception {
        // Given
        when(serviceManagementService.getRequestDetails("REQ-000")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/services/requests/REQ-000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").value(containsString("Request not found with number")));
    }


    @Test
    void testUpdateServiceStatus() throws Exception {
        mockMvc.perform(patch("/api/services/1/service-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Service updated successfully"));
    }

    @Test
    void testUpdateServiceById() throws Exception {
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setEnglishName("Updated Service");
        serviceDTO.setArabicName("خدمة محدثة");
        serviceDTO.setDescription("Service Description");
        serviceDTO.setArabicDescription("وصف الخدمة");
        serviceDTO.setStatusId(1L);
        serviceDTO.setServiceTypeId(1L);
        serviceDTO.setSlaTime(5);

        mockMvc.perform(patch("/api/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Service updated successfully"));
    }

    @Test
    void testGetAllServices() throws Exception {
        // Create a list of ServiceResponseDTO objects for the mock response
        List<ServiceResponseDTO> serviceList = new ArrayList<>();
        ServiceResponseDTO service1 = new ServiceResponseDTO();
        service1.setId(1L);
        service1.setEnglishName("Test Service 1");
        service1.setArabicName("خدمة اختبار 1");
        service1.setDescription("Test Description 1");
        service1.setArabicDescription("وصف الاختبار 1");
        service1.setStatusId(1L);
        service1.setStatusEn("Active");
        service1.setStatusAr("نشط");
        service1.setServiceTypeId(1L);
        service1.setServiceTypeNameEn("Type 1");
        service1.setServiceTypeNameAr("النوع 1");
        service1.setSlaTime(5);

        ServiceResponseDTO service2 = new ServiceResponseDTO();
        service2.setId(2L);
        service2.setEnglishName("Test Service 2");
        service2.setArabicName("خدمة اختبار 2");
        service2.setDescription("Test Description 2");
        service2.setArabicDescription("وصف الاختبار 2");
        service2.setStatusId(2L);
        service2.setStatusEn("Inactive");
        service2.setStatusAr("غير نشط");
        service2.setServiceTypeId(2L);
        service2.setServiceTypeNameEn("Type 2");
        service2.setServiceTypeNameAr("النوع 2");
        service2.setSlaTime(10);

        serviceList.add(service1);
        serviceList.add(service2);

        // Create a Page object with the service list
        Page<ServiceResponseDTO> servicePage = new PageImpl<>(serviceList);

        // Mock the service method to return the Page
        Mockito.when(serviceManagementService.getAllServiceDTOs(any(Pageable.class)))
               .thenReturn(servicePage);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/api/services/all")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].englishName").value("Test Service 1"))
                .andExpect(jsonPath("$.data.content[0].arabicName").value("خدمة اختبار 1"))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].englishName").value("Test Service 2"))
                .andExpect(jsonPath("$.data.content[1].arabicName").value("خدمة اختبار 2"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0));
    }

    @Test
    void testGetLookups() throws Exception {
        ServiceStatus mockStatus = Mockito.mock(ServiceStatus.class);
        Mockito.when(mockStatus.getStatusEn()).thenReturn("Active");

        Mockito.when(serviceManagementService.getAllServiceStatuses())
                .thenReturn(List.of(mockStatus));

        mockMvc.perform(get("/api/services/lookups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.statuses[0].statusEn").value("Active"));
    }

    @Test
    void testGetRequestStatusSummary() throws Exception {
        Mockito.when(requestService.getRequestStatusSummary())
                .thenReturn(List.of(new RequestStatusSummary("Pending","قيد الانتظار", 1L)));


        mockMvc.perform(get("/api/services/requests-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void testUpdateRequestStatus() throws Exception {
        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(Request.Status.valueOf("APPROVED"));
        req.setComment("Looks good");

        mockMvc.perform(patch("/api/services/REQ-123/request-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Request status updated successfully"));
    }

    @Test
    void testUpdateRequestStatus_NotFound() throws Exception {
        Mockito.doThrow(new BusinessException("Error updating request status: REQ-123 not found"))
                .when(requestService)
                .updateRequestStatus(eq("REQ-123"), any(Request.Status.class), anyString());

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(Request.Status.REJECTED);
        req.setComment("Invalid");

        mockMvc.perform(patch("/api/services/REQ-123/request-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data").value("Error updating request status: REQ-123 not found"));
    }


    @Test
    void testGetAllRequests() throws Exception {
        Page<RequestResponseDTO> page = new PageImpl<>(List.of(new RequestResponseDTO()));
        Mockito.when(requestService.getAllRequests(any())).thenReturn(page);

        mockMvc.perform(get("/api/services/all-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testUpdateRequest() throws Exception {
        UpdateRequestDTO dto = new UpdateRequestDTO();
        dto.setDescription("Updated details");

        mockMvc.perform(patch("/api/services/requests/REQ-321")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Request updated successfully"));
    }

    @Test
    void testUpdateRequest_NotFound() throws Exception {
        Mockito.doThrow(new BusinessException("Error updating request: Request not found"))
                .when(requestService).updateRequest(eq("REQ-321"), any());

        UpdateRequestDTO dto = new UpdateRequestDTO();
        dto.setDescription("Failure test");

        mockMvc.perform(patch("/api/services/requests/REQ-321")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data").value("Error updating request: Request not found"));
    }

    @Test
    void testGetAllTemplates() throws Exception {
        Mockito.when(serviceManagementService.getAllTemplates())
                .thenReturn(List.of(new Template()));

        mockMvc.perform(get("/api/services/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetAllSubs() throws Exception {
        Page<TemplateFieldValue> page = new PageImpl<>(List.of(new TemplateFieldValue()));
        Mockito.when(serviceManagementService.getSubmissionsByCustomerNumber(eq("SUB-111"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/services/submissions/SUB-111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testGetCustomerAllSubs() throws Exception {
        TemplateFieldValue mockValue = new TemplateFieldValue();
        mockValue.setId(1L);
        mockValue.setGroupId(100L);
        mockValue.setValue("Test Value");
        mockValue.setSessionId("session-xyz");
        mockValue.setCustomerNumber("CUST-001");
        mockValue.setField(null);

        Page<TemplateFieldValue> mockPage = new PageImpl<>(List.of(mockValue));

        Mockito.when(serviceManagementService.getSubmissionsByCustomerNumber(eq("CUST-001"), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/services/submissions/CUST-001")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].groupId").value(100))
                .andExpect(jsonPath("$.data.content[0].value").value("Test Value"))
                .andExpect(jsonPath("$.data.content[0].sessionId").value("session-xyz"))
                .andExpect(jsonPath("$.data.content[0].customerNumber").value("CUST-001"));
    }

    @Test
    void testAddServiceValidationFailure() throws Exception {
        // Create a service DTO with missing required fields
        ServiceDTO serviceDTO = new ServiceDTO();
        // Not setting required fields like EnglishName, ArabicName, etc.

        mockMvc.perform(post("/api/services/add-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.EnglishName").exists())
                .andExpect(jsonPath("$.data.ArabicName").exists())
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.ArabicDescription").exists())
                .andExpect(jsonPath("$.data.statusId").exists())
                .andExpect(jsonPath("$.data.serviceTypeId").exists());
    }

    @Test
    void testUpdateRequestStatusValidationFailure() throws Exception {
        // Create a status update request with null status (which is required)
        StatusUpdateRequest req = new StatusUpdateRequest();
        // Not setting the required status field
        req.setComment("This should fail validation");

        mockMvc.perform(patch("/api/services/REQ-123/request-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.status").exists());
    }

    @Test
    void testCreateTemplateWithFieldsValidationFailure() throws Exception {
        // Create a template request with missing required fields
        CreateTemplateWithFieldsRequestDTO requestDTO = new CreateTemplateWithFieldsRequestDTO();
        // Not setting required fields

        mockMvc.perform(post("/api/services/templateWithFields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.englishName").exists())
                .andExpect(jsonPath("$.data.arabicName").exists())
                .andExpect(jsonPath("$.data.fields").exists());
    }

    @Test
    void testUpdateRequestWithInvalidData() throws Exception {
        // Create an update request with invalid data (exceeding max length)
        UpdateRequestDTO dto = new UpdateRequestDTO();
        // Set description that exceeds the max length of 500 characters
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            longDescription.append("0123456789");
        }
        dto.setDescription(longDescription.toString());

        mockMvc.perform(patch("/api/services/requests/REQ-321")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.description").exists());
    }

    @Test
    void testResourceNotFoundExceptionHandling() throws Exception {
        // Mock service to throw ResourceNotFoundException
        Mockito.when(serviceManagementService.getService(999L))
                .thenThrow(new ResourceNotFoundException("Service", "id", 999L));

        mockMvc.perform(get("/api/services/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.data").value("Service not found with id : '999'"));
    }

    @Test
    void testBusinessExceptionHandling() throws Exception {
        // Mock service to throw BusinessException
        Mockito.when(serviceManagementService.getAllServiceDTOs(any(Pageable.class)))
                .thenThrow(new BusinessException("Business rule violated"));

        mockMvc.perform(get("/api/services/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data").value("Business rule violated"));
    }
}
