package com.ABIC.CustomerRequest.mobile.requestManagmentService.service;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.AddRequestDTO;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.RequestStatusSummary;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.UpdateRequestDto;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.repository.RequestRepository;
import com.ABIC.CustomerRequest.web.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.ServiceTypeRepository;
import com.ABIC.CustomerRequest.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

@Service
public class RequestService {
    private final RequestRepository requestRepository;
    private final JWTUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;
    private final ServiceTypeRepository serviceTypeRepository;
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    public RequestService(RequestRepository requestRepository, JWTUtil jwtUtil, HttpServletRequest httpServletRequest, ServiceTypeRepository serviceTypeRepository) {
        this.requestRepository = requestRepository;
        this.jwtUtil = jwtUtil;
        this.httpServletRequest = httpServletRequest;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    public Page<Request> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable);
    }

    public Page<Request> getAllRequests(String customerNumber, Pageable pageable) {
        return requestRepository.findRequestByCustomerNumber(customerNumber, pageable);
    }

    public Page<Request> getRequestsByStatus(Request.Status status, Pageable pageable) {
        return requestRepository.findByStatus(status, pageable);
    }


    public Optional<Request> getRequestByNumber(String requestNumber) {
        return Optional.ofNullable(requestRepository.findByRequestNumber(requestNumber));
    }

    public Request saveRequest(AddRequestDTO requestDTO, String sessionId, String channelId, String clientVersion) {

        Long nextSequence = requestRepository.getNextSequenceId();
        String requestNumber = "REQ-" + String.format("%06d", nextSequence);

        Request savedRequest = new Request();
        savedRequest.setRequestNumber(requestNumber);
        savedRequest.setSequenceId(nextSequence);

        savedRequest.setRequestedBy(requestDTO.getRequestedBy());
        savedRequest.setCustomerNumber(requestDTO.getCustomerNumber());
        savedRequest.setDescription(requestDTO.getDescription());
        savedRequest.setTime(LocalDateTime.now());
        savedRequest.setServiceType(requestDTO.getServiceType());
        savedRequest.setStatus(Request.Status.PENDING);

        savedRequest.setChannelId(channelId);
        savedRequest.setClientVersion(clientVersion);
        savedRequest.setSessionId(sessionId);
        return requestRepository.save(savedRequest);
    }

    public Request updateRequestStatus(String requestNumber, Request.Status status,String comment) {
        Request request = requestRepository.findByRequestNumber(requestNumber);
        if (request != null) {
            request.setStatus(status);
            request.setStatusUpdatedBy(
                    jwtUtil.extractName(extractTokenFromCookie("jwtToken"))
            );
            request.setComment(comment);
            return requestRepository.save(request);
        }
        throw new RuntimeException("Request not found with number: " + requestNumber);
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


    public Request updateRequest(String requestNumber, UpdateRequestDto requestDTO) {
        Request request = requestRepository.findByRequestNumber(requestNumber);
        if (request == null) {
            throw new RuntimeException("Request not found with number: " + requestNumber);
        }
        Optional.ofNullable(requestDTO.getCustomerNumber()).filter(desc -> !desc.isEmpty()).ifPresent(request::setCustomerNumber);
        Optional.ofNullable(requestDTO.getDescription()).filter(desc -> !desc.isEmpty()).ifPresent(request::setDescription);
        Optional.ofNullable(requestDTO.getRequestedBy()).filter(reqBy -> !reqBy.isEmpty()).ifPresent(request::setRequestedBy);
        Optional.ofNullable(requestDTO.getStatusUpdatedBy()).filter(statusBy -> !statusBy.isEmpty()).ifPresent(request::setStatusUpdatedBy);
        Optional.ofNullable(requestDTO.getStatus())
                .filter(status -> !status.isEmpty())
                .ifPresent(status -> request.setStatus(Request.Status.valueOf(status)));
        Optional.ofNullable(requestDTO.getServiceType())
                .filter(service -> !service.isEmpty())
                .ifPresent(service -> request.setServiceType(requestDTO.getServiceType()));

        Optional.ofNullable(requestDTO.getSlaTime()).ifPresent(request::setSlaTime);

        return requestRepository.save(request);
    }

//    public String getServiceDetails(int id) {
//        ServiceType serviceType = serviceTypeRepository.findById((long) id)
//                .orElseThrow(() -> new RuntimeException("ServiceType not found with ID: " + id));
//
//        return serviceType.getDetailsEn() != null ? serviceType.getDetailsEn() : "No details available";
//    }

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

//    public boolean validateSession(ValidateRequest validateRequest) {
//        String validationUrl = "http://10.38.2.15:8091/MubasherRESTAPI/api/RestSOA/validateSession";
//
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("HED", Map.of(
//                    "sessionId", validateRequest.getSessionId(),
//                    "clientVersion", validateRequest.getClientVersion(),
//                    "serviceId", validateRequest.getServiceId(),
//                    "userId", validateRequest.getUserId(),
//                    "channelId", validateRequest.getChannelId()
//            ));
//            requestBody.put("DAT", new HashMap<>());
//
//            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(validationUrl, requestEntity, Map.class);
//
//            Map<String, Object> responseBody = responseEntity.getBody();
//            if (responseBody != null && responseBody.containsKey("status")) {
//                int status = (int) responseBody.get("status");
//                return status == 1;
//            }
//
//        } catch (Exception e) {
//            logger.error("Session validation failed: {}", e.getMessage());
//        }
//
//        return false;
//    }


}
