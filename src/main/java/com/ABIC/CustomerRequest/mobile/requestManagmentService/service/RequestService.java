package com.ABIC.CustomerRequest.mobile.requestManagmentService.service;

import com.ABIC.CustomerRequest.exception.BusinessException;
import com.ABIC.CustomerRequest.exception.ResourceNotFoundException;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.ValidateRequest;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.AddRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.RequestStatusSummary;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.RequestResponseDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.dto.UpdateRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.repository.RequestRepository;
import com.ABIC.CustomerRequest.web.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.web.serviceManagment.model.Services;
import com.ABIC.CustomerRequest.web.serviceManagment.model.TemplateField;
import com.ABIC.CustomerRequest.web.serviceManagment.model.TemplateFieldValue;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.FieldValueDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.TemplateSubmissionDTO;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.ServiceRepository;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.ServiceTypeRepository;
import com.ABIC.CustomerRequest.util.JWTUtil;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.TemplateFieldValueRepository;
import com.ABIC.CustomerRequest.web.serviceManagment.service.ServiceManagementService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.web.client.RestTemplate;

@Service
public class RequestService {
    private final RequestRepository requestRepository;
    private final JWTUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;
    private final ServiceTypeRepository serviceTypeRepository;
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
    private final ServiceManagementService serviceManagementService;
    private final TemplateFieldValueRepository templateFieldValueRepository;
    private final ServiceRepository serviceRepository;

    public RequestService(RequestRepository requestRepository, JWTUtil jwtUtil, HttpServletRequest httpServletRequest, ServiceTypeRepository serviceTypeRepository, ServiceManagementService serviceManagementService, TemplateFieldValueRepository templateFieldValueRepository, ServiceRepository serviceRepository) {
        this.requestRepository = requestRepository;
        this.jwtUtil = jwtUtil;
        this.httpServletRequest = httpServletRequest;
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceManagementService = serviceManagementService;
        this.templateFieldValueRepository = templateFieldValueRepository;
        this.serviceRepository = serviceRepository;
    }

    public Page<RequestResponseDTO> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    public Page<RequestResponseDTO> getAllRequests(String customerNumber, Pageable pageable) {
        return requestRepository.findRequestByCustomerNumber(customerNumber, pageable)
                .map(this::mapToDTO);
    }

    public Page<RequestResponseDTO> getRequestsByStatus(Request.Status status, Pageable pageable) {
        return requestRepository.findByStatus(status, pageable)
                .map(this::mapToDTO);
    }

    public Page<RequestResponseDTO> findByStatusAndCustomerNumber(Request.Status status, String customerNumber, Pageable pageable) {
        return requestRepository.findByStatusAndCustomerNumber(status, customerNumber, pageable)
                .map(this::mapToDTO);
    }



    public Optional<Request> getRequestByNumber(String requestNumber) {
        return Optional.ofNullable(requestRepository.findByRequestNumber(requestNumber));
    }

    public Request saveRequest(AddRequestDTO requestDTO, String sessionId, String channelId, String clientVersion) {
        String requestNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        Services service = serviceRepository.findById(requestDTO.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", requestDTO.getServiceId()));

        Request savedRequest = new Request();
        savedRequest.setRequestNumber(requestNumber);
        savedRequest.setRequestedBy(requestDTO.getRequestedBy());
        savedRequest.setCustomerNumber(requestDTO.getCustomerNumber());
        savedRequest.setDescription(requestDTO.getDescription());
        savedRequest.setTime(LocalDateTime.now());
        savedRequest.setService(service);
        savedRequest.setStatus(Request.Status.PENDING);

        savedRequest.setSlaTime(service.getSlaTime());

        savedRequest.setChannelId(channelId);
        savedRequest.setClientVersion(clientVersion);
        savedRequest.setSessionId(sessionId);

        try {
            return requestRepository.save(savedRequest);
        } catch (Exception e) {
            logger.error("Error saving request: {}", e.getMessage());
            throw new BusinessException("Failed to save request: " + e.getMessage(), e);
        }
    }


    public Request updateRequestStatus(String requestNumber, Request.Status status, String comment) {
        Request request = requestRepository.findByRequestNumber(requestNumber);
        if (request == null) {
            throw new ResourceNotFoundException("Request", "number", requestNumber);
        }

        try {
            request.setStatus(status);
            String updatedBy = jwtUtil.extractName(extractTokenFromCookie("jwtToken"));
            if (updatedBy == null) {
                throw new BusinessException("Unable to extract user from token");
            }
            request.setStatusUpdatedBy(updatedBy);
            request.setComment(comment);
            return requestRepository.save(request);
        } catch (ResourceNotFoundException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating request status: {}", e.getMessage());
            throw new BusinessException("Failed to update request status: " + e.getMessage(), e);
        }
    }


    public List<RequestStatusSummary> getRequestStatusSummary() {
        List<String> allStatuses = List.of("PENDING", "APPROVED", "REJECTED");

        Map<String, String> statusTranslations = Map.of(
                "PENDING", "قيد الانتظار",
                "APPROVED", "موافق عليه",
                "REJECTED", "مرفوض"
        );

        List<Object[]> queryResults = requestRepository.countRequestsByStatus();

        Map<String, Long> statusCounts = queryResults.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString().toUpperCase(),
                        result -> (long) result[1]
                ));

        return allStatuses.stream()
                .map(status -> new RequestStatusSummary(
                        status,
                        statusTranslations.getOrDefault(status, "غير معروف"),
                        statusCounts.getOrDefault(status, 0L)
                ))
                .collect(Collectors.toList());
    }


    public Request updateRequest(String requestNumber, UpdateRequestDTO requestDTO) {
        Request request = requestRepository.findByRequestNumber(requestNumber);
        if (request == null) {
            throw new ResourceNotFoundException("Request", "number", requestNumber);
        }

        try {
            Optional.ofNullable(requestDTO.getCustomerNumber()).filter(desc -> !desc.isEmpty()).ifPresent(request::setCustomerNumber);
            Optional.ofNullable(requestDTO.getDescription()).filter(desc -> !desc.isEmpty()).ifPresent(request::setDescription);
            Optional.ofNullable(requestDTO.getRequestedBy()).filter(reqBy -> !reqBy.isEmpty()).ifPresent(request::setRequestedBy);
            Optional.ofNullable(requestDTO.getStatusUpdatedBy()).filter(statusBy -> !statusBy.isEmpty()).ifPresent(request::setStatusUpdatedBy);

            Optional.ofNullable(requestDTO.getStatus())
                    .filter(status -> !status.isEmpty())
                    .ifPresent(status -> {
                        try {
                            request.setStatus(Request.Status.valueOf(status));
                        } catch (IllegalArgumentException e) {
                            throw new BusinessException("Invalid status value: " + status);
                        }
                    });

            Optional.ofNullable(requestDTO.getServiceId())
                    .ifPresent(serviceId -> {
                        Services service = serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));
                        request.setService(service);
                        request.setSlaTime(service.getSlaTime());
                    });

            return requestRepository.save(request);
        } catch (ResourceNotFoundException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating request: {}", e.getMessage());
            throw new BusinessException("Failed to update request: " + e.getMessage(), e);
        }
    }

    private RequestResponseDTO mapToDTO(Request request) {
        return new RequestResponseDTO(
                request.getRequestNumber(),
                request.getService() != null ? request.getService().getId() : null,
                request.getRequestedBy(),
                request.getCustomerNumber(),
                request.getTime(),
                request.getSlaTime(),
                request.getStatusUpdatedBy(),
                request.getStatus() != null ? request.getStatus().name() : null
        );
    }

    public String getServiceDetails(int id) {
        ServiceType serviceType = serviceTypeRepository.findById((long) id)
                .orElseThrow(() -> new RuntimeException("ServiceType not found with ID: " + id));

        return serviceType.getDetailsEn() != null ? serviceType.getDetailsEn() : "No details available";
    }

    private String extractTokenFromCookie(String cookieName) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        } else {
            System.out.println("No cookies present in the request.");
        }
        return null;
    }

    public List<ServiceType> getAllServiceTypes() {return serviceTypeRepository.findAll();}

    public String submitTemplateForm(TemplateSubmissionDTO submissionDTO, String sessionId, String customerNumber) {
        if (submissionDTO.getValues() == null || submissionDTO.getValues().isEmpty()) {
            throw new BusinessException("No values provided in the submission");
        }

        try {
            for (FieldValueDTO dto : submissionDTO.getValues()) {
                TemplateField field = serviceManagementService.getTemplateFieldById(dto.getFieldId());

                if (field == null) {
                    throw new ResourceNotFoundException("Template Field", "id", dto.getFieldId());
                }

                TemplateFieldValue value = new TemplateFieldValue();
                value.setField(field);
                value.setGroupId(submissionDTO.getGroupId());
                value.setValue(dto.getValue());
                value.setSessionId(sessionId);
                value.setCustomerNumber(customerNumber);
                templateFieldValueRepository.save(value);
            }

            return "submitted successfully";
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error submitting template form: {}", e.getMessage());
            throw new BusinessException("Failed to submit template form: " + e.getMessage(), e);
        }
    }


    public boolean validateSession(ValidateRequest validateRequest) {
        String validationUrl = "http://10.38.2.17:8080/MubasherRESTAPI/api/RestSOA/validateSession";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("HED", Map.of(
                    "sessionId", validateRequest.getSessionId(),
                    "clientVersion", validateRequest.getClientVersion(),
                    "serviceId", validateRequest.getServiceId(),
                    "userId", validateRequest.getUserId(),
                    "channelId", validateRequest.getChannelId()
            ));
            requestBody.put("DAT", new HashMap<>());

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(validationUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseBody != null && responseBody.containsKey("DAT")) {
                Map<String, Object> dat = (Map<String, Object>) responseBody.get("DAT");
                if (dat != null && dat.containsKey("status")) {
                    int status = (int) dat.get("status");
                    return status == 1;
                }
            }


        } catch (Exception e) {
            logger.error("Session validation failed: {}", e.getMessage());
        }

        return false;
    }


}
