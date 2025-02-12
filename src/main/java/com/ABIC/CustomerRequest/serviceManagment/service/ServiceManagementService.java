package com.ABIC.CustomerRequest.serviceManagment.service;

import com.ABIC.CustomerRequest.serviceManagment.model.ServiceDTO;
import com.ABIC.CustomerRequest.serviceManagment.model.ServiceStatus;
import com.ABIC.CustomerRequest.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.serviceManagment.repository.ServiceRepository;
import com.ABIC.CustomerRequest.serviceManagment.model.Services;
import com.ABIC.CustomerRequest.serviceManagment.repository.ServiceStatusRepository;
import com.ABIC.CustomerRequest.serviceManagment.repository.ServiceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;

    private final ServiceTypeRepository serviceTypeRepository;

    private final ServiceStatusRepository serviceStatusRepository;

    public ServiceManagementService(ServiceRepository serviceRepository, ServiceTypeRepository serviceTypeRepository, ServiceStatusRepository serviceStatusRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceStatusRepository = serviceStatusRepository;
    }

    public Services createService(ServiceDTO dto) {
        // Fetch service type
        ServiceType serviceType = serviceTypeRepository.findById(dto.getServiceTypeId())
                .orElseThrow(() -> new RuntimeException("Service Type not found"));

        ServiceStatus status = serviceStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));


        Services service = new Services();
        service.setArabicName(dto.getArabicName());
        service.setEnglishName(dto.getEnglishName());
        service.setDescription(dto.getDescription());
        service.setArabicDescription(dto.getArabicDescription());
        service.setPlaceHolder(dto.getPlaceHolder());
        service.setArabicPlaceHolder(dto.getArabicPlaceHolder());
        service.setServiceType(serviceType);
        service.setStatus(status);
        service.setSlaTime(dto.getSlaTime());

        return serviceRepository.save(service);
    }

    public Services updateService(Long serviceId, ServiceDTO service) {
        Services existingService = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));

        ServiceType serviceType = serviceTypeRepository.findById(service.getServiceTypeId())
                .orElseThrow(() -> new RuntimeException("Service Type not found with ID: " + service.getServiceTypeId()));

        ServiceStatus status = serviceStatusRepository.findById(service.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found with ID: " + service.getStatusId()));

        existingService.setArabicName(service.getArabicName());
        existingService.setEnglishName(service.getEnglishName());

        existingService.setStatus(status);
        existingService.setServiceType(serviceType);

        existingService.setDescription(service.getDescription());
        existingService.setArabicDescription(service.getArabicDescription());

        existingService.setSlaTime(service.getSlaTime());

        existingService.setPlaceHolder(service.getPlaceHolder());
        existingService.setArabicPlaceHolder(service.getArabicPlaceHolder());

        serviceRepository.save(existingService);
        return existingService;
    }


    public Page<Services> getAllServices(Pageable pageable) {
        return serviceRepository.findAll(pageable);
    }


    public String toggleServiceVisibility(Long serviceId, Long statusId) {
        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));

        ServiceStatus status = serviceStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found with ID: " + statusId));

        service.setStatus(status);
        serviceRepository.save(service);

        return "Service visibility updated successfully";
    }

    public List<ServiceStatus> getAllServiceStatuses() {
        return serviceStatusRepository.findAll();
    }

    public List<ServiceType> getAllServiceTypes() {
        return serviceTypeRepository.findAll();
    }


}
