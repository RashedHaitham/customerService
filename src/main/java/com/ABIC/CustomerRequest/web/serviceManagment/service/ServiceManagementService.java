package com.ABIC.CustomerRequest.web.serviceManagment.service;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.repository.RequestRepository;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import com.ABIC.CustomerRequest.web.serviceManagment.model.*;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.*;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;

    private final RequestRepository requestRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    private final ServiceStatusRepository serviceStatusRepository;

    private final TemplateRepository templateRepository;

    private final TemplateFieldRepository templateFieldRepository;

    private final TemplateFieldValueRepository templateFieldValueRepository;

    private final ControlTypeLookupRepository controlTypeLookupRepository;


    @Autowired
    public ServiceManagementService(ServiceRepository serviceRepository, RequestRepository requestRepository, ServiceTypeRepository serviceTypeRepository, ServiceStatusRepository serviceStatusRepository, TemplateRepository templateRepository, TemplateFieldRepository templateFieldRepository, TemplateFieldValueRepository templateFieldValueRepository, ControlTypeLookupRepository controlTypeLookupRepository) {
        this.serviceRepository = serviceRepository;
        this.requestRepository = requestRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceStatusRepository = serviceStatusRepository;
        this.templateRepository = templateRepository;
        this.templateFieldRepository = templateFieldRepository;
        this.templateFieldValueRepository = templateFieldValueRepository;
        this.controlTypeLookupRepository = controlTypeLookupRepository;
    }

    public void createService(ServiceDTO dto) {
        ServiceType serviceType = serviceTypeRepository.findById(dto.getServiceTypeId())
                .orElseThrow(() -> new RuntimeException("Service Type not found"));

        ServiceStatus status = serviceStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        Template template = templateRepository.findById(dto.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        Services service = new Services();
        service.setArabicName(dto.getArabicName());
        service.setEnglishName(dto.getEnglishName());
        service.setDescription(dto.getDescription());
        service.setArabicDescription(dto.getArabicDescription());
        service.setServiceType(serviceType);
        service.setStatus(status);
        service.setTemplate(template);
        service.setSlaTime(dto.getSlaTime());

        serviceRepository.save(service);
    }

    public void updateService(Long serviceId, ServiceDTO service) {
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


        Template template = templateRepository.findById(service.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        existingService.setTemplate(template);

        serviceRepository.save(existingService);
    }

    public TemplateField getTemplateFieldById(Long id) {
        try {
            return templateFieldRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching template field by ID: " + e.getMessage());
            return null;
        }
    }


    public Page<Services> getAllServices(Pageable pageable) {
        return serviceRepository.findAll(pageable);
    }

    public Page<Services> getServicesByStatus(String status, Pageable pageable) {
        return serviceRepository.findByStatusEn(status, pageable);
    }

    public void toggleServiceVisibility(Long serviceId, Long statusId) {
        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));

        ServiceStatus status = serviceStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found with ID: " + statusId));

        service.setStatus(status);
        serviceRepository.save(service);

    }

    public List<ServiceStatus> getAllServiceStatuses() {
        return serviceStatusRepository.findAll();
    }

    public List<ServiceType> getAllServiceTypes() {
        return serviceTypeRepository.findAll();
    }

    public List<Template> getAllTemplates() {
        try {
            return templateRepository.findAllByHiddenIsFalse();
        } catch (Exception e) {
            System.err.println("Error fetching templates: " + e.getMessage());
            return List.of();
        }
    }

    public List<TemplateField> getTemplateFieldsByGroupId(Long groupId) {
        try {
            return templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId);
        } catch (Exception e) {
            System.err.println("Error fetching template fields by group ID: " + e.getMessage());
            return List.of();
        }
    }

    public Page<TemplateFieldValue> getSubmissionsByCustomerNumber(String customerNumber, Pageable pageable) {
        return templateFieldValueRepository.getAllByCustomerNumber(customerNumber, pageable);
    }

    public Optional<List<TemplateField>> getAllTemplateFields(String groupId) {
        try {
            Long parsedGroupId = Long.parseLong(groupId);
            List<TemplateField> fields = templateFieldRepository.findByGroupIdAndHiddenIsFalse(parsedGroupId);
            return Optional.ofNullable(fields);
        } catch (Exception e) {
            System.err.println("Error fetching template: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteTemplate(Long templateGroupId) {
        triggerTemplate(templateGroupId,true);
    }

    public void restoreTemplate(Long templateGroupId) {
        triggerTemplate(templateGroupId,false);
    }

    private void triggerTemplate(Long templateGroupId, boolean hidden) {
        Template template = templateRepository.findByGroupId(templateGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with groupId: " + templateGroupId));

        List<TemplateField> fields = templateFieldRepository.findByGroupIdAndHiddenIsFalse(templateGroupId);

        for (TemplateField field : fields) {
            field.setHidden(hidden);
        }

        template.setHidden(hidden);

        templateRepository.save(template);
        templateFieldRepository.saveAll(fields);
    }

    public void deleteTemplateField(Long fieldId) {
        triggerTemplateField(fieldId,true);
    }

    public void restoreTemplateField(Long fieldId) {
        triggerTemplateField(fieldId,false);
    }

    private void triggerTemplateField(Long fieldId, boolean hidden) {
        templateFieldRepository.findById(fieldId).ifPresentOrElse(field -> {
            field.setHidden(hidden);
            templateFieldRepository.save(field);
        }, () -> {
            throw new EntityNotFoundException("Template field not found with ID: " + fieldId);
        });
    }



    @Transactional
    public boolean updateTemplate(Long groupId, UpdateTemplateWithFieldsRequestDTO request) {
        Optional<Template> optionalTemplate = templateRepository.findByGroupId(groupId);

        if (optionalTemplate.isEmpty()) {
            return false;
        }

        Template existingTemplate = updateTemplateDetails(optionalTemplate.get(), request);
        templateRepository.save(existingTemplate);

        Map<Long, TemplateField> existingFieldMap = templateFieldRepository.findByGroupId(groupId).stream()
                .collect(Collectors.toMap(TemplateField::getId, field -> field));

        for (TemplateField incomingField : request.getFields()) {
            if (incomingField.getId() != null && existingFieldMap.containsKey(incomingField.getId())) {
                updateExistingField(existingFieldMap.get(incomingField.getId()), incomingField);
            } else {
                TemplateField newField = createNewField(incomingField, groupId);
                templateFieldRepository.save(newField);
            }
        }

        return true;
    }

    private Template updateTemplateDetails(Template template, UpdateTemplateWithFieldsRequestDTO request) {
        template.setEnglishName(request.getEnglishName());
        template.setArabicName(request.getArabicName());
        template.setEnglishDescription(request.getEnglishDescription());
        template.setArabicDescription(request.getArabicDescription());
        return template;
    }

    private void updateExistingField(TemplateField existingField, TemplateField incomingField) {
        existingField.setLabelEn(incomingField.getLabelEn());
        existingField.setLabelAr(incomingField.getLabelAr());
        existingField.setControlType(incomingField.getControlType());
        existingField.setRequired(incomingField.isRequired());
        existingField.setAttachment(incomingField.isAttachment());
        existingField.setSorting(incomingField.getSorting());
        existingField.setPlaceholderEn(incomingField.getPlaceholderEn());
        existingField.setPlaceholderAr(incomingField.getPlaceholderAr());
        existingField.setExtraDataEn(incomingField.getExtraDataEn());
        existingField.setExtraDataAr(incomingField.getExtraDataAr());

        templateFieldRepository.save(existingField);
    }

    private TemplateField createNewField(TemplateField incomingField, Long groupId) {
        TemplateField newField = new TemplateField();
        newField.setLabelEn(incomingField.getLabelEn());
        newField.setLabelAr(incomingField.getLabelAr());
        newField.setControlType(incomingField.getControlType());
        newField.setRequired(incomingField.isRequired());
        newField.setAttachment(incomingField.isAttachment());
        newField.setSorting(incomingField.getSorting());
        newField.setPlaceholderEn(incomingField.getPlaceholderEn());
        newField.setPlaceholderAr(incomingField.getPlaceholderAr());
        newField.setExtraDataEn(incomingField.getExtraDataEn());
        newField.setExtraDataAr(incomingField.getExtraDataAr());
        newField.setGroupId(groupId);
        return newField;
    }

    public Response<String> createTemplateWithFields(CreateTemplateWithFieldsRequestDTO request) {
        try {
            // Save the template
            Template template = new Template();
            template.setEnglishName(request.getEnglishName());
            template.setArabicName(request.getArabicName());
            template.setEnglishDescription(request.getEnglishDescription());
            template.setArabicDescription(request.getArabicDescription());
            template.setGroupId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);

            Template savedTemplate = templateRepository.save(template);

            for (TemplateField field : request.getFields()) {
                String controlTypeCode = field.getControlType().getCode();

                ControlTypeLookup controlType = (ControlTypeLookup) controlTypeLookupRepository.findByCode(controlTypeCode)
                        .orElseThrow(() -> new RuntimeException("ControlType with code [" + controlTypeCode + "] not found"));

                field.setControlType(controlType);
                field.setGroupId(savedTemplate.getGroupId());

                templateFieldRepository.save(field);
            }


            return ResponseUtils.success(HttpStatus.CREATED.value(), "Template and fields created successfully");

        } catch (Exception e) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create template: " + e.getMessage());
        }
    }


    public Optional<List<ControlTypeLookup>> getControlTypes() {
        try {
            return Optional.of(controlTypeLookupRepository.findAll());
        } catch (Exception e) {
            System.err.println("Error fetching control types: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Page<ServiceResponseDTO> getAllServiceDTOs(Pageable pageable) {
        Page<Services> servicesPage = serviceRepository.findAll(pageable);
        return servicesPage.map(this::mapToDTO);
    }

    private ServiceResponseDTO mapToDTO(Services service) {
        Template template = service.getTemplate();
        ServiceType serviceType = service.getServiceType();
        ServiceStatus status = service.getStatus();

        return new ServiceResponseDTO(
                service.getId(),
                service.getArabicName(),
                service.getEnglishName(),
                service.getDescription(),
                service.getArabicDescription(),

                template.getId(),
                template.getGroupId(),
                template.getEnglishName(),
                template.getArabicName(),

                serviceType.getId(),
                serviceType.getTypeEn(),
                serviceType.getTypeAr(),


                status.getId(),
                status.getStatusAr(),
                status.getStatusEn(),

                service.getSlaTime()
        );
    }

    public Optional<RequestDetailsDTO> getRequestDetails(String requestNumber) {
        return requestRepository.findById(requestNumber)
                .map(request -> {
                    List<TemplateFieldValue> fieldValues = templateFieldValueRepository
                            .findByCustomerNumber( request.getCustomerNumber());

                    List<FieldValueDTO> fields = fieldValues.stream()
                            .map(fv -> new FieldValueDTO(fv.getField().getId(),fv.getField().getLabelAr() ,fv.getField().getLabelEn(), fv.getValue()))
                            .toList();

                    return new RequestDetailsDTO(
                            request.getRequestNumber(),
                            request.getDescription(),
                            request.getRequestedBy(),
                            request.getCustomerNumber(),
                            request.getStatusUpdatedBy(),
                            request.getServiceType(),
                            request.getStatus().name(),
                            request.getTime(),
                            request.getSlaTime(),
                            request.getComment(),
                            fields
                    );
                });
    }




}
