package com.ABIC.CustomerRequest.requestManagmentService.service;

import com.ABIC.CustomerRequest.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.requestManagmentService.model.RequestDTO;
import com.ABIC.CustomerRequest.requestManagmentService.model.RequestStatusSummary;
import com.ABIC.CustomerRequest.requestManagmentService.repository.RequestRepository;
import com.ABIC.CustomerRequest.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RequestService {
    private final RequestRepository requestRepository;
    private final JWTUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;

    public RequestService(RequestRepository requestRepository, JWTUtil jwtUtil, HttpServletRequest httpServletRequest) {
        this.requestRepository = requestRepository;
        this.jwtUtil = jwtUtil;
        this.httpServletRequest = httpServletRequest;
    }

    public Page<Request> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable);
    }

    public Page<Request> getRequestsByStatus(Request.Status status, Pageable pageable) {
        return requestRepository.findByStatus(status, pageable);
    }


    public Optional<Request> getRequestByNumber(String requestNumber) {
        return Optional.ofNullable(requestRepository.findByRequestNumber(requestNumber));
    }

    public Request saveRequest(RequestDTO requestDTO) {
        String token = extractTokenFromCookie("jwtToken");

        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or missing JWT token");
        }
        //String email = jwtUtil.extractEmail(token);
        String name = jwtUtil.extractName(token);
        String customerNumber = jwtUtil.extractUserNumber(token);

        Request savedRequest = new Request();
        savedRequest.setRequestedBy(name);
        savedRequest.setCustomerNumber(customerNumber);
        savedRequest.setDescription(requestDTO.getDescription());
        savedRequest.setTime(LocalDateTime.now());
        savedRequest.setSlaTime(requestDTO.getSlaTime());
        savedRequest.setServiceType(requestDTO.getServiceType());
        savedRequest.setStatus(Request.Status.PENDING);

        return requestRepository.save(savedRequest);
    }

    //@PreAuthorize("hasRole('ADMIN')")
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


    public Request updateRequest(String requestNumber, RequestDTO requestDTO) {
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
        Optional.ofNullable(requestDTO.getServiceType()).filter(serviceType -> !serviceType.isEmpty()).ifPresent(request::setServiceType);
        Optional.ofNullable(requestDTO.getSlaTime()).ifPresent(request::setSlaTime);

        return requestRepository.save(request);
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
}
