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

import java.util.*;
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

    public List<TemplateFieldDTO> getTemplateFieldsByGroupId(String groupId) {
        try {
            List<TemplateField> fields = templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId);

            return fields.stream()
                    .map(field -> new TemplateFieldDTO(
                            null,
                            field.getLabelEn(),
                            field.getLabelAr(),
                            field.getControlType().getCode(),
                            field.isRequired(),
                            field.isAttachment(),
                            field.getSorting(),
                            field.getPlaceholderEn(),
                            field.getPlaceholderAr(),
                            convertStringToList(field.getExtraDataEn()),
                            convertStringToList(field.getExtraDataAr())
                    ))
                    .collect(Collectors.toList());


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
            List<TemplateField> fields = templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId);
            return Optional.ofNullable(fields);
        } catch (Exception e) {
            System.err.println("Error fetching template: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteTemplate(String templateGroupId) {
        triggerTemplate(templateGroupId,true);
    }

    public void restoreTemplate(String templateGroupId) {
        triggerTemplate(templateGroupId,false);
    }

    private void triggerTemplate(String templateGroupId, boolean hidden) {
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
    public boolean updateTemplate(String groupId, UpdateTemplateWithFieldsRequestDTO request) {
        Optional<Template> optionalTemplate = templateRepository.findByGroupId(groupId);

        if (optionalTemplate.isEmpty()) {
            return false;
        }

        Template existingTemplate = updateTemplateDetails(optionalTemplate.get(), request);
        templateRepository.save(existingTemplate);

        Map<Long, TemplateField> existingFieldMap = templateFieldRepository.findByGroupId(groupId).stream()
                .collect(Collectors.toMap(TemplateField::getId, field -> field));

        Set<Long> incomingIds = new HashSet<>();

        for (TemplateFieldDTO incomingField : request.getFields()) {
            //update existing field
            if (incomingField.getId() != null && existingFieldMap.containsKey(incomingField.getId())) {
                updateExistingField(existingFieldMap.get(incomingField.getId()), incomingField);
                incomingIds.add(incomingField.getId());
            }
            //create new field
            else {
                TemplateField newField = createNewField(incomingField, groupId);
                templateFieldRepository.save(newField);
            }
        }

        existingFieldMap.keySet().stream()
                .filter(existingId -> !incomingIds.contains(existingId))
                .forEach(this::deleteTemplateField);

        return true;
    }

    private Template updateTemplateDetails(Template template, UpdateTemplateWithFieldsRequestDTO request) {
        template.setEnglishName(request.getEnglishName());
        template.setArabicName(request.getArabicName());
        template.setEnglishDescription(request.getEnglishDescription());
        template.setArabicDescription(request.getArabicDescription());
        return template;
    }

    private void updateExistingField(TemplateField existingField, TemplateFieldDTO dto) {
        ControlTypeLookup controlType = controlTypeLookupRepository.findByCode(dto.getControlType())
                .orElseThrow(() -> new RuntimeException("Invalid control type: " + dto.getControlType()));

        existingField.setLabelEn(dto.getLabelEn());
        existingField.setLabelAr(dto.getLabelAr());
        existingField.setControlType(controlType);
        existingField.setRequired(dto.isRequired());
        existingField.setAttachment(dto.isAttachment());
        existingField.setSorting(dto.getSorting());
        existingField.setPlaceholderEn(dto.getPlaceholderEn());
        existingField.setPlaceholderAr(dto.getPlaceholderAr());
        existingField.setExtraDataEn(convertListToString(dto.getExtraDataEn()));
        existingField.setExtraDataAr(convertListToString(dto.getExtraDataAr()));

        templateFieldRepository.save(existingField);
    }

    private TemplateField createNewField(TemplateFieldDTO dto, String groupId) {
        ControlTypeLookup controlType = controlTypeLookupRepository.findByCode(dto.getControlType())
                .orElseThrow(() -> new RuntimeException("Invalid control type: " + dto.getControlType()));

        TemplateField newField = new TemplateField();
        newField.setLabelEn(dto.getLabelEn());
        newField.setLabelAr(dto.getLabelAr());
        newField.setControlType(controlType);
        newField.setRequired(dto.isRequired());
        newField.setAttachment(dto.isAttachment());
        newField.setSorting(dto.getSorting());
        newField.setPlaceholderEn(dto.getPlaceholderEn());
        newField.setPlaceholderAr(dto.getPlaceholderAr());
        newField.setExtraDataEn(convertListToString(dto.getExtraDataEn()));
        newField.setExtraDataAr(convertListToString(dto.getExtraDataAr()));
        newField.setGroupId(groupId);
        return newField;
    }

    @Transactional
    public Response<String> createTemplateWithFields(CreateTemplateWithFieldsRequestDTO request) {
        try {
            // Save the template
            Template template = new Template();
            template.setEnglishName(request.getEnglishName());
            template.setArabicName(request.getArabicName());
            template.setEnglishDescription(request.getEnglishDescription());
            template.setArabicDescription(request.getArabicDescription());
            template.setGroupId(UUID.randomUUID().toString());

            Template savedTemplate = templateRepository.save(template);

            for (TemplateFieldDTO dto : request.getFields()) {

                String controlTypeCode = dto.getControlType();
                if (controlTypeCode == null || controlTypeCode.isBlank()) {
                    throw new RuntimeException("ControlType code is required for all fields.");
                }

                ControlTypeLookup controlType = controlTypeLookupRepository.findByCode(controlTypeCode)
                        .orElseThrow(() -> new RuntimeException("ControlType with code [" + controlTypeCode + "] not found"));

                TemplateField field = new TemplateField();
                field.setLabelEn(dto.getLabelEn());
                field.setLabelAr(dto.getLabelAr());
                field.setControlType(controlType);
                field.setRequired(dto.isRequired());
                field.setAttachment(dto.isAttachment());
                field.setSorting(dto.getSorting());
                field.setPlaceholderEn(dto.getPlaceholderEn());
                field.setPlaceholderAr(dto.getPlaceholderAr());
                field.setGroupId(savedTemplate.getGroupId());
                field.setHidden(false);

                field.setExtraDataEn(dto.getExtraDataEn() != null ? String.join(",", dto.getExtraDataEn()) : null);
                field.setExtraDataAr(dto.getExtraDataAr() != null ? String.join(",", dto.getExtraDataAr()) : null);

                templateFieldRepository.save(field);
            }

            return ResponseUtils.success(HttpStatus.CREATED.value(), "Template and fields created successfully with group id "+savedTemplate.getGroupId());

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
                            .findByCustomerNumber( request.getUserId());

                    List<FieldValueDTO> fields = fieldValues.stream()
                            .map(fv -> {
                                TemplateField field = fv.getField();
                                return new FieldValueDTO(
                                        field.getId(),
                                        field.getLabelAr(),
                                        field.getLabelEn(),
                                        fv.getValue(),
                                        convertStringToList(field.getExtraDataEn()),
                                        convertStringToList(field.getExtraDataAr())
                                );
                            })
                            .collect(Collectors.toList());



                    return new RequestDetailsDTO(
                            request.getRequestNumber(),
                            request.getDescription(),
                            request.getRequestedBy(),
                            request.getUserId(),
                            request.getStatusUpdatedBy(),
                            request.getService().getId(),
                            request.getStatus().name(),
                            request.getTime(),
                            request.getSlaTime(),
                            request.getComment(),
                            fields
                    );
                });
    }

    private List<String> convertStringToList(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String convertListToString(List<String> list) {
        return (list == null || list.isEmpty()) ? null : String.join(",", list);
    }
}
