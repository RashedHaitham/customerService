package com.ABIC.CustomerRequest.web.serviceManagment.controller;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.*;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.UpdateRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.service.RequestService;
import com.ABIC.CustomerRequest.web.serviceManagment.model.*;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.ServiceDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.service.ServiceManagementService;
import com.ABIC.CustomerRequest.util.PaginatedResponse;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceManagementController {

    private final ServiceManagementService serviceManagementService;
    private final RequestService requestService;

    public ServiceManagementController(ServiceManagementService serviceManagementService, RequestService requestService) {
        this.serviceManagementService = serviceManagementService;
        this.requestService = requestService;
    }

    @PostMapping("/add-service")
    public ResponseEntity<Response<String>> addService(@RequestBody ServiceDTO service) {
        serviceManagementService.createService(service);
        Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Service added successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{serviceId}/status")
    public ResponseEntity<Response<String>> updateService(
            @PathVariable Long serviceId,
            @RequestBody Map<String, Object> payload) {
        Long status = (Long) payload.get("statusId");
        serviceManagementService.toggleServiceVisibility(serviceId, status);
        Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Service updated successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{serviceId}")
    public ResponseEntity<Response<String>> updateService(@PathVariable Long serviceId, @RequestBody ServiceDTO service) {
        serviceManagementService.updateService(serviceId, service);
        Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Service updated successfully");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/all")
    public ResponseEntity<Response<PaginatedResponse<Services>>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Services> servicesPage = serviceManagementService.getAllServices(pageable);

        PaginatedResponse<Services> responseData = new PaginatedResponse<>(servicesPage);

        Response<PaginatedResponse<Services>> response = ResponseUtils.success(HttpStatus.OK.value(), responseData);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/lookups")
    public ResponseEntity<Response<Map<String, List<?>>>> getLookups() {
        List<ServiceStatus> statuses = serviceManagementService.getAllServiceStatuses();
        List<ServiceType> types = serviceManagementService.getAllServiceTypes();

        Map<String, List<?>> data = Map.of(
                "statuses", statuses,
                "types", types
        );

        Response<Map<String, List<?>>> response = ResponseUtils.success(HttpStatus.OK.value(), data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests-summary")
    public ResponseEntity<Map<String, Object>> getRequestStatusSummary() {
        List<RequestStatusSummary> requests = requestService.getRequestStatusSummary();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("statusCode", HttpStatus.OK.value());
        response.put("data", requests);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{requestNumber}/status")
    public ResponseEntity<Response<String>> updateRequestStatus(
            @PathVariable String requestNumber,
            @RequestBody StatusUpdateRequest statusUpdateRequest
    ) {

        try {
            requestService.updateRequestStatus(requestNumber, statusUpdateRequest.getStatus(), statusUpdateRequest.getComment());
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Request status updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Response<String> errorResponse = ResponseUtils.error(HttpStatus.NOT_FOUND.value(), "Request not found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/all-requests")
    public ResponseEntity<Response<PaginatedResponse<Request>>> getAllRequests(
            @RequestParam(required = false) Request.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Request> requestsPage;

        if (status != null) {
            requestsPage = requestService.getRequestsByStatus(status, pageable);
        } else {
            requestsPage = requestService.getAllRequests(pageable);
        }

        PaginatedResponse<Request> responseData = new PaginatedResponse<>(requestsPage);

        Response<PaginatedResponse<Request>> response = ResponseUtils.success(HttpStatus.OK.value(), responseData);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{requestNumber}")
    public ResponseEntity<Response<String>> updateRequest(
            @PathVariable String requestNumber,
            @RequestBody UpdateRequestDTO requestDTO
    ) {

        try {
            requestService.updateRequest(requestNumber, requestDTO);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Request updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Response<String> errorResponse = ResponseUtils.error(HttpStatus.NOT_FOUND.value(), "Request not found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/templates/{groupId}")
    public ResponseEntity<Response<List<TemplateField>>> getTemplatesByGroupId(@PathVariable Long groupId) {
        List<TemplateField> templates = serviceManagementService.getTemplateFieldsByGroupId(groupId);
        Response<List<TemplateField>> response = ResponseUtils.success(HttpStatus.OK.value(), templates);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/templates")
    public ResponseEntity<Response<List<Template>>> getAllTemplates() {
        List<Template> templates = serviceManagementService.getAllTemplates();
        Response<List<Template>> response = ResponseUtils.success(HttpStatus.OK.value(), templates);
        return ResponseEntity.ok(response);
    }

}