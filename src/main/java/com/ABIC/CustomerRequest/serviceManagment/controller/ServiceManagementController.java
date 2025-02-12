package com.ABIC.CustomerRequest.serviceManagment.controller;

import com.ABIC.CustomerRequest.serviceManagment.model.ServiceDTO;
import com.ABIC.CustomerRequest.serviceManagment.model.ServiceStatus;
import com.ABIC.CustomerRequest.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.serviceManagment.model.Services;
import com.ABIC.CustomerRequest.serviceManagment.service.ServiceManagementService;
import com.ABIC.CustomerRequest.util.PaginatedResponse;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceManagementController {

    private final ServiceManagementService serviceManagementService;

    public ServiceManagementController(ServiceManagementService serviceManagementService) {
        this.serviceManagementService = serviceManagementService;
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


}