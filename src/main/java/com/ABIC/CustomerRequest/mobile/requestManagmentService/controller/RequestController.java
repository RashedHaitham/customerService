package com.ABIC.CustomerRequest.mobile.requestManagmentService.controller;

import com.ABIC.CustomerRequest.exception.BusinessException;
import com.ABIC.CustomerRequest.exception.ResourceNotFoundException;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.*;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.AddRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.RequestResponseDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.service.RequestService;
import com.ABIC.CustomerRequest.util.PaginatedResponse;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import jakarta.validation.Valid;
import com.ABIC.CustomerRequest.web.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.web.serviceManagment.model.Services;
import com.ABIC.CustomerRequest.web.serviceManagment.model.TemplateField;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.TemplateFieldDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.TemplateSubmissionDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.service.ServiceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
    public static final List<String> ACCEPTED_CHANNEL_IDS = Arrays.asList("12", "23", "34", "45", "56");

    private final ServiceManagementService serviceManagementService;
    private final RequestService requestService;
    public RequestController(ServiceManagementService serviceManagementService, RequestService requestService) {
        this.serviceManagementService = serviceManagementService;
        this.requestService = requestService;
    }

    @GetMapping("/all/{customerNumber}")
    public ResponseEntity<Response<PaginatedResponse<RequestResponseDTO>>> getAllRequests(
            @RequestHeader("Session-Id") String sessionId,
            @RequestHeader("Client-Version") String clientVersion,
            @RequestHeader("Channel-Id") String channelId,
            @RequestHeader("Service-Id") String serviceId,
            @PathVariable String customerNumber,
            @RequestParam(required = false) Request.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            if (!ACCEPTED_CHANNEL_IDS.contains(channelId)) {
                throw new BusinessException("Invalid Channel-Id: " + channelId);
            }

            if (customerNumber == null || customerNumber.trim().isEmpty()) {
                throw new BusinessException("Customer number cannot be empty");
            }

            ValidateRequest validateRequest = new ValidateRequest(
                    sessionId,
                    clientVersion,
                    serviceId,
                    customerNumber,
                    channelId
            );

            // Should validate session and userId
            // Commented out for now, but would use BusinessException instead
            // if (!requestService.validateSession(validateRequest)) {
            //     throw new BusinessException("Invalid session ID");
            // }

            Pageable pageable = PageRequest.of(page, size);
            Page<RequestResponseDTO> requestsPage;

            if (status != null) {
                requestsPage = requestService.findByStatusAndCustomerNumber(status, customerNumber, pageable);
            } else {
                requestsPage = requestService.getAllRequests(customerNumber, pageable);
            }

            if (requestsPage.isEmpty()) {
                logger.info("No requests found for customer: {}, status: {}", customerNumber, status);
            }

            PaginatedResponse<RequestResponseDTO> responseData = new PaginatedResponse<>(requestsPage);
            return ResponseEntity.ok(ResponseUtils.success(HttpStatus.OK.value(), responseData));
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving requests: {}", e.getMessage());
            throw new BusinessException("Error retrieving requests: " + e.getMessage(), e);
        }
    }




    @PostMapping("/add-request")
    public ResponseEntity<Response<String>> createRequest(
            @Valid @RequestBody RequestWrapper requestWrapper
    ) {
        ValidateRequest validateRequest = requestWrapper.getValidateRequest();
        AddRequestDTO req = requestWrapper.getAddRequestDTO();

        String channelId = validateRequest.getChannelId();
        String clientVersion = validateRequest.getClientVersion();
        String sessionId = validateRequest.getSessionId();

        if (!ACCEPTED_CHANNEL_IDS.contains(channelId)) {
            throw new BusinessException("Invalid Channel-Id: " + channelId);
        }

//        if (!requestService.validateSession(validateRequest)) {
//            throw new BusinessException("Invalid session ID");
//        }

        try {
            logger.info("Received request creation: {}, Channel: {}, Client-Version: {}", req, channelId, clientVersion);
            requestService.saveRequest(req, sessionId, channelId, clientVersion);
            logger.info("Request created successfully from Channel: {}, Client-Version: {}", channelId, clientVersion);

            return ResponseEntity.ok(ResponseUtils.success(HttpStatus.OK.value(), "Request created successfully"));
        } catch (ResourceNotFoundException e) {
            // Let the global exception handler handle this
            throw e;
        } catch (Exception e) {
            logger.error("Error creating request: {}, Channel: {}, Client-Version: {}", e.getMessage(), channelId, clientVersion, e);
            throw new BusinessException("Error creating request: " + e.getMessage(), e);
        }
    }

    @GetMapping("/serviceTypes")
    public ResponseEntity<Response<List<ServiceType>>> getServiceTypes() {
        try {
            List<ServiceType> serviceTypes = requestService.getAllServiceTypes();

            if (serviceTypes.isEmpty()) {
                logger.warn("No service types found");
            }

            return ResponseEntity.ok(ResponseUtils.success(HttpStatus.OK.value(), serviceTypes));
        } catch (Exception e) {
            logger.error("Error retrieving service types: {}", e.getMessage());
            throw new BusinessException("Error retrieving service types: " + e.getMessage(), e);
        }
    }

    @GetMapping("/services")
    public ResponseEntity<Response<PaginatedResponse<Services>>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            if (page < 0) {
                throw new BusinessException("Page number cannot be negative");
            }

            if (size <= 0) {
                throw new BusinessException("Page size must be greater than zero");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Services> servicesPage = serviceManagementService.getServicesByStatus("Active", pageable);

            if (servicesPage.isEmpty()) {
                logger.info("No active services found");
            }

            PaginatedResponse<Services> responseData = new PaginatedResponse<>(servicesPage);
            Response<PaginatedResponse<Services>> response = ResponseUtils.success(HttpStatus.OK.value(), responseData);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving services: {}", e.getMessage());
            throw new BusinessException("Error retrieving services: " + e.getMessage(), e);
        }
    }

    @GetMapping("/templateFields/{groupId}")
    public ResponseEntity<Response<List<TemplateField>>> getAllTemplateFields(@PathVariable String groupId) {
        try {
            if (groupId == null || groupId.trim().isEmpty()) {
                throw new BusinessException("Group ID cannot be empty");
            }

            Optional<List<TemplateField>> optionalTemplates = serviceManagementService.getAllTemplateFields(groupId);

            if (optionalTemplates.isPresent() && !optionalTemplates.get().isEmpty()) {
                Response<List<TemplateField>> response = ResponseUtils.success(HttpStatus.OK.value(), optionalTemplates.get());
                return ResponseEntity.ok(response);
            } else {
                throw new ResourceNotFoundException("Template fields", "group ID", groupId);
            }
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving template fields: {}", e.getMessage());
            throw new BusinessException("Error retrieving template fields: " + e.getMessage(), e);
        }
    }
    @PostMapping("/service/submit")
    public ResponseEntity<Response<String>> submitForm(
            @Valid @RequestBody TemplateSubmissionDTO submissionDTO) {

//        if (!requestService.validateSession(submissionDTO.getValidateRequest())) {
//            throw new BusinessException("Invalid session ID");
//        }

        try {
            // Check if validateRequest is null
            if (submissionDTO.getValidateRequest() == null) {
                throw new BusinessException("ValidateRequest cannot be null");
            }

            String submissionId = requestService.submitTemplateForm(submissionDTO,
                    submissionDTO.getValidateRequest().getSessionId(), submissionDTO.getValidateRequest().getUserId());
            return ResponseEntity.ok(
                    ResponseUtils.success(HttpStatus.OK.value(), "Form submitted successfully. Submission ID: " + submissionId));
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error submitting form: {}", e.getMessage());
            throw new BusinessException("Error submitting form: " + e.getMessage(), e);
        }
    }

    @GetMapping("/templates/{groupId}")
    public ResponseEntity<Response<List<TemplateFieldDTO>>> getTemplateFieldsByGroupId(@PathVariable String groupId) {
        try {
            if (groupId == null || groupId.trim().isEmpty()) {
                throw new BusinessException("Group ID cannot be empty");
            }

            List<TemplateFieldDTO> templates = serviceManagementService.getTemplateFieldsByGroupId(groupId);

            if (templates.isEmpty()) {
                logger.warn("No template fields found for group ID: {}", groupId);
            }

            Response<List<TemplateFieldDTO>> response = ResponseUtils.success(HttpStatus.OK.value(), templates);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving template fields: {}", e.getMessage());
            throw new BusinessException("Error retrieving template fields: " + e.getMessage(), e);
        }
    }

}
