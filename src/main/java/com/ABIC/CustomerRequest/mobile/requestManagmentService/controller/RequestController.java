package com.ABIC.CustomerRequest.mobile.requestManagmentService.controller;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.*;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.AddRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.service.RequestService;
import com.ABIC.CustomerRequest.util.PaginatedResponse;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import com.ABIC.CustomerRequest.web.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.web.serviceManagment.model.Services;
import com.ABIC.CustomerRequest.web.serviceManagment.model.TemplateField;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.TemplateFieldDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.TemplateSubmissionDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.service.ServiceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Response<PaginatedResponse<Request>>> getAllRequests(
            @RequestHeader("Session-Id") String sessionId,
            @PathVariable String customerNumber,
            @RequestParam(required = false) Request.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Request> requestsPage;

//        if (!requestService.validateSession(sessionId)) {
//            Response<PaginatedResponse<Request>> errorResponse =
//                    ResponseUtils.error(HttpStatus.FORBIDDEN.value(), null);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
//        }

        if (status != null) {
            requestsPage = requestService.findByStatusAndCustomerNumber(status,customerNumber, pageable);
        } else {
            requestsPage = requestService.getAllRequests(customerNumber,pageable);
        }

        PaginatedResponse<Request> responseData = new PaginatedResponse<>(requestsPage);

        Response<PaginatedResponse<Request>> response = ResponseUtils.success(HttpStatus.OK.value(), responseData);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/add-request")
    public ResponseEntity<Response<String>> createRequest(
            @RequestBody RequestWrapper requestWrapper
    ) {
        ValidateRequest validateRequest = requestWrapper.getValidateRequest();
        AddRequestDTO req = requestWrapper.getAddRequestDTO();

        String channelId = validateRequest.getChannelId();
        String clientVersion = validateRequest.getClientVersion();
        String sessionId = validateRequest.getSessionId();

        if (!ACCEPTED_CHANNEL_IDS.contains(channelId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseUtils.error(HttpStatus.FORBIDDEN.value(), "Invalid Channel-Id"));
        }

//        if (!requestService.validateSession(validateRequest)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ResponseUtils.error(HttpStatus.UNAUTHORIZED.value(), "Invalid session ID"));
//        }

        try {
            logger.info("Received request creation: {}, Channel: {}, Client-Version: {}", req, channelId, clientVersion);
            requestService.saveRequest(req,sessionId, channelId, clientVersion);
            logger.info("Request created successfully from Channel: {}, Client-Version: {}", channelId, clientVersion);

            return ResponseEntity.ok(ResponseUtils.success(HttpStatus.OK.value(), "Request created successfully"));
        } catch (Exception e) {
            logger.error("Error creating request: {}, Channel: {}, Client-Version: {}", e.getMessage(), channelId, clientVersion, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), "Error creating request: " + e.getMessage()));
        }
    }

    @GetMapping("/serviceTypes")
    public ResponseEntity<Map<String, Object>> getServiceTypes() {
        List<ServiceType> serviceTypes = requestService.getAllServiceTypes();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("statusCode", HttpStatus.OK.value());
        response.put("data", serviceTypes);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/services")
    public ResponseEntity<Response<PaginatedResponse<Services>>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Services> servicesPage = serviceManagementService.getServicesByStatus("Active", pageable);

        PaginatedResponse<Services> responseData = new PaginatedResponse<>(servicesPage);

        Response<PaginatedResponse<Services>> response = ResponseUtils.success(HttpStatus.OK.value(), responseData);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/service/submit")
    public ResponseEntity<String> submitForm(@RequestBody TemplateSubmissionDTO submissionDTO,@RequestHeader("sessionId") String sessionId) {

        //customer number should be retrieved via sessionId
        String submissionId = requestService.submitTemplateForm(submissionDTO,sessionId,"1234");
        return ResponseEntity.ok("Form submitted successfully. Submission ID: " + submissionId);
    }

    @GetMapping("/templates/{groupId}")
    public ResponseEntity<Response<List<TemplateFieldDTO>>> getTemplateFieldsByGroupId(@PathVariable String groupId) {
        List<TemplateFieldDTO> templates = serviceManagementService.getTemplateFieldsByGroupId(groupId);
        Response<List<TemplateFieldDTO>> response = ResponseUtils.success(HttpStatus.OK.value(), templates);
        return ResponseEntity.ok(response);
    }

}

