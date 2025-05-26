package com.ABIC.CustomerRequest.web.serviceManagment.controller;

import com.ABIC.CustomerRequest.exception.BusinessException;
import com.ABIC.CustomerRequest.exception.ResourceNotFoundException;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.*;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.RequestResponseDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.UpdateRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.service.RequestService;
import com.ABIC.CustomerRequest.web.serviceManagment.model.*;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.*;
import com.ABIC.CustomerRequest.web.serviceManagment.service.ServiceManagementService;
import com.ABIC.CustomerRequest.util.PaginatedResponse;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.*;

@RestController
@RequestMapping("/api/services")
public class ServiceManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManagementController.class);
    private final ServiceManagementService serviceManagementService;
    private final RequestService requestService;

    public ServiceManagementController(ServiceManagementService serviceManagementService, RequestService requestService) {
        this.serviceManagementService = serviceManagementService;
        this.requestService = requestService;
    }

    @PostMapping("/add-service")
    public ResponseEntity<Response<String>> addService(@Valid @RequestBody ServiceDTO service) {
        try {
            serviceManagementService.createService(service);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Service added successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error adding service: {}", e.getMessage());
            throw new BusinessException("Error adding service: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<Response<ServiceResponseDTO>> getService(@PathVariable Long serviceId) {
        try {
            ServiceResponseDTO service = serviceManagementService.getService(serviceId);
            Response<ServiceResponseDTO> response = ResponseUtils.success(HttpStatus.OK.value(), service);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving service: {}", e.getMessage());
            throw new BusinessException("Error retrieving service: " + e.getMessage(), e);
        }
    }

    /**
     * Endpoint to update the service status.
     * <p>
     * Example Request Body:
     * {
     * "statusId": 1
     * }
     *
     * @param serviceId The ID of the service to be updated.
     * @param payload   The request body containing the status ID.
     * @return ResponseEntity with success status and message.
     */
    @PatchMapping("/{serviceId}/service-status")
    public ResponseEntity<Response<String>> updateService(
            @PathVariable Long serviceId,
            @RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("statusId")) {
                throw new BusinessException("Status ID is required");
            }

            Object statusObj = payload.get("statusId");
            if (statusObj == null) {
                throw new BusinessException("Status ID cannot be null");
            }

            Long status;
            try {
                status = statusObj instanceof Integer
                        ? ((Integer) statusObj).longValue()
                        : (Long) statusObj;
            } catch (ClassCastException e) {
                throw new BusinessException("Invalid status ID format");
            }

            serviceManagementService.toggleServiceVisibility(serviceId, status);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Service updated successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error updating service status: {}", e.getMessage());
            throw new BusinessException("Error updating service status: " + e.getMessage(), e);
        }
    }

    @PatchMapping("/{serviceId}")
    public ResponseEntity<Response<String>> updateService(@PathVariable Long serviceId, @Valid @RequestBody ServiceDTO service) {
        try {
            serviceManagementService.updateService(serviceId, service);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Service updated successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error updating service: {}", e.getMessage());
            throw new BusinessException("Error updating service: " + e.getMessage(), e);
        }
    }


    @GetMapping("/all")
    public ResponseEntity<Response<PaginatedResponse<ServiceResponseDTO>>> getAllServices(
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
            Page<ServiceResponseDTO> dtoPage = serviceManagementService.getAllServiceDTOs(pageable);

            if (dtoPage.isEmpty()) {
                logger.info("No services found");
            }

            PaginatedResponse<ServiceResponseDTO> responseData = new PaginatedResponse<>(dtoPage);
            return ResponseEntity.ok(ResponseUtils.success(HttpStatus.OK.value(), responseData));
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving services: {}", e.getMessage());
            throw new BusinessException("Error retrieving services: " + e.getMessage(), e);
        }
    }




    @GetMapping("/lookups")
    public ResponseEntity<Response<Map<String, List<?>>>> getLookups() {
        try {
            List<ServiceStatus> statuses = serviceManagementService.getAllServiceStatuses();
            List<ServiceType> types = serviceManagementService.getAllServiceTypes();
            List<ControlTypeLookup> controlTypes = serviceManagementService.getControlTypes().orElse(Collections.emptyList());

            if (statuses.isEmpty()) {
                logger.warn("No service statuses found");
            }

            if (types.isEmpty()) {
                logger.warn("No service types found");
            }

            if (controlTypes.isEmpty()) {
                logger.warn("No control types found");
            }

            Map<String, List<?>> data = Map.of(
                    "controlTypes", controlTypes,
                    "statuses", statuses,
                    "types", types
            );

            Response<Map<String, List<?>>> response = ResponseUtils.success(HttpStatus.OK.value(), data);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving lookups: {}", e.getMessage());
            throw new BusinessException("Error retrieving lookups: " + e.getMessage(), e);
        }
    }

    @GetMapping("/requests-summary")
    public ResponseEntity<Response<List<RequestStatusSummary>>> getRequestStatusSummary() {
        try {
            List<RequestStatusSummary> requests = requestService.getRequestStatusSummary();

            if (requests.isEmpty()) {
                logger.warn("No request status summary found");
            }

            return ResponseEntity.ok(ResponseUtils.success(HttpStatus.OK.value(), requests));
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving request status summary: {}", e.getMessage());
            throw new BusinessException("Error retrieving request status summary: " + e.getMessage(), e);
        }
    }

    @PatchMapping("/{requestNumber}/request-status")
    public ResponseEntity<Response<String>> updateRequestStatus(
            @PathVariable String requestNumber,
            @Valid @RequestBody StatusUpdateRequest statusUpdateRequest
    ) {
        try {
            requestService.updateRequestStatus(requestNumber, statusUpdateRequest.getStatus(), statusUpdateRequest.getComment());
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Request status updated successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error updating request status: {}", e.getMessage());
            throw new BusinessException("Error updating request status: " + e.getMessage(), e);
        }
    }

    @GetMapping("/requests/{requestNumber}")
    public ResponseEntity<Response<RequestDetailsDTO>> getRequestByNumber(@PathVariable String requestNumber) {
        try {
            if (requestNumber == null || requestNumber.trim().isEmpty()) {
                throw new BusinessException("Request number cannot be empty");
            }

            return serviceManagementService.getRequestDetails(requestNumber)
                    .map(details -> {
                        Response<RequestDetailsDTO> response = ResponseUtils.success(HttpStatus.OK.value(), details);
                        return ResponseEntity.ok(response);
                    })
                    .orElseThrow(() -> new ResourceNotFoundException("Request", "number", requestNumber));
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving request details: {}", e.getMessage());
            throw new BusinessException("Error retrieving request details: " + e.getMessage(), e);
        }
    }


    @GetMapping("/all-requests")
    public ResponseEntity<Response<PaginatedResponse<RequestResponseDTO>>> getAllRequests(
            @RequestParam(required = false) Request.Status status,
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
            Page<RequestResponseDTO> requestsPage;

            if (status != null) {
                requestsPage = requestService.getRequestsByStatus(status, pageable);
            } else {
                requestsPage = requestService.getAllRequests(pageable);
            }

            if (requestsPage.isEmpty()) {
                logger.info("No requests found with status: {}", status);
            }

            PaginatedResponse<RequestResponseDTO> responseData = new PaginatedResponse<>(requestsPage);
            Response<PaginatedResponse<RequestResponseDTO>> response = ResponseUtils.success(HttpStatus.OK.value(), responseData);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving requests: {}", e.getMessage());
            throw new BusinessException("Error retrieving requests: " + e.getMessage(), e);
        }
    }

    @PatchMapping("/requests/{requestNumber}")
    public ResponseEntity<Response<String>> updateRequest(
            @PathVariable String requestNumber,
            @Valid @RequestBody UpdateRequestDTO requestDTO
    ) {
        try {
            if (requestNumber == null || requestNumber.trim().isEmpty()) {
                throw new BusinessException("Request number cannot be empty");
            }

            requestService.updateRequest(requestNumber, requestDTO);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Request updated successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error updating request: {}", e.getMessage());
            throw new BusinessException("Error updating request: " + e.getMessage(), e);
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<Response<Page<Template>>> getAllTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<Template> templates = serviceManagementService.getAllTemplates(page, size);

            if (templates.isEmpty()) {
                logger.warn("No templates found");
            }

            Response<Page<Template>> response = ResponseUtils.success(HttpStatus.OK.value(), templates);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving templates: {}", e.getMessage());
            throw new BusinessException("Error retrieving templates: " + e.getMessage(), e);
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

    @GetMapping("/submissions/{customerNumber}")
    public ResponseEntity<Response<Page<TemplateFieldValue>>> getCustomerAllSubs(@PathVariable String customerNumber,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        try {
            if (customerNumber == null || customerNumber.trim().isEmpty()) {
                throw new BusinessException("Customer number cannot be empty");
            }

            if (page < 0) {
                throw new BusinessException("Page number cannot be negative");
            }

            if (size <= 0) {
                throw new BusinessException("Page size must be greater than zero");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<TemplateFieldValue> templates = serviceManagementService.getSubmissionsByCustomerNumber(customerNumber, pageable);

            if (templates.isEmpty()) {
                logger.info("No submissions found for customer: {}", customerNumber);
            }

            Response<Page<TemplateFieldValue>> response = ResponseUtils.success(HttpStatus.OK.value(), templates);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving customer submissions: {}", e.getMessage());
            throw new BusinessException("Error retrieving customer submissions: " + e.getMessage(), e);
        }
    }

    @PostMapping("/templateWithFields")
    public ResponseEntity<Response<String>> createTemplateWithFields(@Valid @RequestBody CreateTemplateWithFieldsRequestDTO request) {
        try {
            Response<String> response = serviceManagementService.createTemplateWithFields(request);

            if (response.getStatusCode() == HttpStatus.CREATED.value()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error creating template with fields: {}", e.getMessage());
            throw new BusinessException("Error creating template with fields: " + e.getMessage(), e);
        }
    }


    @DeleteMapping("/template/{templateGroupId}")
    public ResponseEntity<Response<String>> deleteTemplate(@PathVariable String templateGroupId) {
        try {
            if (templateGroupId == null || templateGroupId.trim().isEmpty()) {
                throw new BusinessException("Template group ID cannot be empty");
            }

            serviceManagementService.deleteTemplate(templateGroupId);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Template deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting template: {}", e.getMessage());
            throw new BusinessException("Error deleting template: " + e.getMessage(), e);
        }
    }

    @PatchMapping("/template/{groupId}")
    public ResponseEntity<Response<String>> updateTemplate(@PathVariable String groupId, @Valid @RequestBody UpdateTemplateWithFieldsRequestDTO request) {
        try {
            if (groupId == null || groupId.trim().isEmpty()) {
                throw new BusinessException("Group ID cannot be empty");
            }

            boolean updated = serviceManagementService.updateTemplate(groupId, request);

            if (!updated) {
                throw new ResourceNotFoundException("Template", "group ID", groupId);
            }

            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Template and Fields updated successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error updating template: {}", e.getMessage());
            throw new BusinessException("Error updating template: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/template-field/{fieldId}")
    public ResponseEntity<Response<String>> deleteTemplateField(@PathVariable Long fieldId) {
        try {
            if (fieldId == null) {
                throw new BusinessException("Field ID cannot be null");
            }

            serviceManagementService.deleteTemplateField(fieldId);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Template field deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting template field: {}", e.getMessage());
            throw new BusinessException("Error deleting template field: " + e.getMessage(), e);
        }
    }

    @PatchMapping("/template/restore/{groupId}")
    public ResponseEntity<Response<String>> restoreTemplate(@PathVariable String groupId) {
        try {
            if (groupId == null || groupId.trim().isEmpty()) {
                throw new BusinessException("Group ID cannot be empty");
            }

            serviceManagementService.restoreTemplate(groupId);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Template activated successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error restoring template: {}", e.getMessage());
            throw new BusinessException("Error restoring template: " + e.getMessage(), e);
        }
    }

    @PatchMapping("/template-field/restore/{fieldId}")
    public ResponseEntity<Response<String>> restoreTemplateField(@PathVariable Long fieldId) {
        try {
            if (fieldId == null) {
                throw new BusinessException("Field ID cannot be null");
            }

            serviceManagementService.restoreTemplateField(fieldId);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Template field activated successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BusinessException e) {
            // Let the global exception handler handle these
            throw e;
        } catch (Exception e) {
            logger.error("Error restoring template field: {}", e.getMessage());
            throw new BusinessException("Error restoring template field: " + e.getMessage(), e);
        }
    }
}
