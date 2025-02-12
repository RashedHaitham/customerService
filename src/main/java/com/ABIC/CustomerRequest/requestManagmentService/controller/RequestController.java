package com.ABIC.CustomerRequest.requestManagmentService.controller;

import com.ABIC.CustomerRequest.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.requestManagmentService.model.RequestDTO;
import com.ABIC.CustomerRequest.requestManagmentService.model.RequestStatusSummary;
import com.ABIC.CustomerRequest.requestManagmentService.model.StatusUpdateRequest;
import com.ABIC.CustomerRequest.requestManagmentService.service.RequestService;
import com.ABIC.CustomerRequest.util.PaginatedResponse;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/all")
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



    @PostMapping("/add-request")
    public ResponseEntity<Response<String>> createRequest(@RequestBody RequestDTO req) {
        try {
            requestService.saveRequest(req);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Request created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<String> errorResponse = ResponseUtils.error(HttpStatus.BAD_REQUEST.value(), "Error creating request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @GetMapping("/{requestNumber}")
    public ResponseEntity<Response<Request>> getRequestByNumber(@PathVariable String requestNumber) {
        return requestService.getRequestByNumber(requestNumber)
                .map(request -> {
                    Response<Request> response = ResponseUtils.success(HttpStatus.OK.value(), request);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Response<Request> errorResponse = ResponseUtils.error(HttpStatus.NOT_FOUND.value(), null);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }


    @PatchMapping("/{requestNumber}/status")
    public ResponseEntity<Response<String>> updateRequestStatus(
            @PathVariable String requestNumber,
            @RequestBody StatusUpdateRequest statusUpdateRequest) {
        try {
            Request.Status status = statusUpdateRequest.getStatus();
            String comment = statusUpdateRequest.getComment();

            requestService.updateRequestStatus(requestNumber, status, comment);

            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Request status updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Response<String> errorResponse = ResponseUtils.error(HttpStatus.NOT_FOUND.value(), "Request not found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    @PatchMapping("/{requestNumber}")
    public ResponseEntity<Response<String>> updateRequest(
            @PathVariable String requestNumber,
            @RequestBody RequestDTO requestDTO) {
        try {
            requestService.updateRequest(requestNumber, requestDTO);
            Response<String> response = ResponseUtils.success(HttpStatus.OK.value(), "Request updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Response<String> errorResponse = ResponseUtils.error(HttpStatus.NOT_FOUND.value(), "Request not found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getRequestStatusSummary() {
        List<RequestStatusSummary> requests = requestService.getRequestStatusSummary();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("statusCode", HttpStatus.OK.value());
        response.put("data", requests);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

