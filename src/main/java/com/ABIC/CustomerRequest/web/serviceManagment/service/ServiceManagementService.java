package com.ABIC.CustomerRequest.web.serviceManagment.service;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.repository.RequestRepository;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.util.ResponseUtils;
import com.ABIC.CustomerRequest.web.serviceManagment.model.*;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.*;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManagementService.class);

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
        logger.info("Creating new service with name: {}", dto.getEnglishName());
        try {
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
            logger.info("Service created successfully with name: {}", dto.getEnglishName());
        } catch (Exception e) {
            logger.error("Error creating service: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void updateService(Long serviceId, ServiceDTO service) {
        logger.info("Updating service with ID: {}", serviceId);
        try {
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
            logger.info("Service with ID: {} updated successfully", serviceId);
        } catch (Exception e) {
            logger.error("Error updating service with ID {}: {}", serviceId, e.getMessage(), e);
            throw e;
        }
    }

    public TemplateField getTemplateFieldById(Long id) {
        logger.info("Fetching template field by ID: {}", id);
        try {
            TemplateField field = templateFieldRepository.findById(id).orElse(null);
            if (field != null) {
                logger.info("Template field found with ID: {}", id);
            } else {
                logger.info("No template field found with ID: {}", id);
            }
            return field;
        } catch (Exception e) {
            logger.error("Error fetching template field by ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    public Page<Services> getAllServices(Pageable pageable) {
        logger.info("Fetching all services with pagination");
        try {
            Page<Services> services = serviceRepository.findAll(pageable);
            logger.info("Retrieved {} services", services.getTotalElements());
            return services;
        } catch (Exception e) {
            logger.error("Error fetching all services: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<Services> getServicesByStatus(String status, Pageable pageable) {
        logger.info("Fetching services by status: {}", status);
        try {
            Page<Services> services = serviceRepository.findByStatusEn(status, pageable);
            logger.info("Retrieved {} services with status: {}", services.getTotalElements(), status);
            return services;
        } catch (Exception e) {
            logger.error("Error fetching services by status {}: {}", status, e.getMessage(), e);
            throw e;
        }
    }

    public void toggleServiceVisibility(Long serviceId, Long statusId) {
        logger.info("Toggling service visibility for service ID: {} with status ID: {}", serviceId, statusId);
        try {
            Services service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));

            ServiceStatus status = serviceStatusRepository.findById(statusId)
                    .orElseThrow(() -> new RuntimeException("Status not found with ID: " + statusId));

            service.setStatus(status);
            serviceRepository.save(service);
            logger.info("Service visibility toggled successfully for service ID: {} to status: {}", serviceId, status.getStatusEn());
        } catch (Exception e) {
            logger.error("Error toggling service visibility for service ID: {} with status ID: {}: {}", serviceId, statusId, e.getMessage(), e);
            throw e;
        }
    }

    public List<ServiceStatus> getAllServiceStatuses() {
        logger.info("Fetching all service statuses");
        try {
            List<ServiceStatus> statuses = serviceStatusRepository.findAll();
            logger.info("Retrieved {} service statuses", statuses.size());
            return statuses;
        } catch (Exception e) {
            logger.error("Error fetching all service statuses: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ServiceType> getAllServiceTypes() {
        logger.info("Fetching all service types");
        try {
            List<ServiceType> types = serviceTypeRepository.findAll();
            logger.info("Retrieved {} service types", types.size());
            return types;
        } catch (Exception e) {
            logger.error("Error fetching all service types: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<Template> getAllTemplates(int page, int size) {
        logger.info("Fetching page {} of size {} for non-hidden templates", page, size);
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<Template> templates = templateRepository.findAllByHiddenIsFalse(pageable);
            logger.info("Retrieved {} templates", templates.getNumberOfElements());
            return templates;
        } catch (Exception e) {
            logger.error("Error fetching templates: {}", e.getMessage(), e);
            return Page.empty();
        }
    }


    public List<TemplateFieldDTO> getTemplateFieldsByGroupId(String groupId) {
        logger.info("Fetching template fields by group ID: {}", groupId);
        try {
            List<TemplateField> fields = templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId);

            List<TemplateFieldDTO> fieldDTOs = fields.stream()
                    .map(field -> new TemplateFieldDTO(
                            field.getId(),
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

            logger.info("Retrieved {} template fields for group ID: {}", fieldDTOs.size(), groupId);
            return fieldDTOs;
        } catch (Exception e) {
            logger.error("Error fetching template fields by group ID {}: {}", groupId, e.getMessage(), e);
            return List.of();
        }
    }

    public Page<TemplateFieldValue> getSubmissionsByCustomerNumber(String customerNumber, Pageable pageable) {
        logger.info("Fetching submissions by customer number: {}", customerNumber);
        try {
            Page<TemplateFieldValue> submissions = templateFieldValueRepository.getAllByCustomerNumber(customerNumber, pageable);
            logger.info("Retrieved {} submissions for customer number: {}", submissions.getTotalElements(), customerNumber);
            return submissions;
        } catch (Exception e) {
            logger.error("Error fetching submissions for customer number {}: {}", customerNumber, e.getMessage(), e);
            throw e;
        }
    }

    public Optional<List<TemplateField>> getAllTemplateFields(String groupId) {
        logger.info("Fetching all template fields for group ID: {}", groupId);
        try {
            List<TemplateField> fields = templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId);
            if (fields != null && !fields.isEmpty()) {
                logger.info("Retrieved {} template fields for group ID: {}", fields.size(), groupId);
            } else {
                logger.info("No template fields found for group ID: {}", groupId);
            }
            return Optional.ofNullable(fields);
        } catch (Exception e) {
            logger.error("Error fetching template fields for group ID {}: {}", groupId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public void deleteTemplate(String templateGroupId) {
        logger.info("Deleting template with group ID: {}", templateGroupId);
        try {
            triggerTemplate(templateGroupId, true);
            logger.info("Template with group ID: {} deleted successfully", templateGroupId);
        } catch (Exception e) {
            logger.error("Error deleting template with group ID {}: {}", templateGroupId, e.getMessage(), e);
            throw e;
        }
    }

    public void restoreTemplate(String templateGroupId) {
        logger.info("Restoring template with group ID: {}", templateGroupId);
        try {
            triggerTemplate(templateGroupId, false);
            logger.info("Template with group ID: {} restored successfully", templateGroupId);
        } catch (Exception e) {
            logger.error("Error restoring template with group ID {}: {}", templateGroupId, e.getMessage(), e);
            throw e;
        }
    }

    private void triggerTemplate(String templateGroupId, boolean hidden) {
        logger.debug("Triggering template with group ID: {} to hidden: {}", templateGroupId, hidden);
        try {
            Template template = templateRepository.findByGroupId(templateGroupId)
                    .orElseThrow(() -> new EntityNotFoundException("Template not found with groupId: " + templateGroupId));

            List<TemplateField> fields = templateFieldRepository.findByGroupIdAndHiddenIsFalse(templateGroupId);
            logger.debug("Found {} fields for template group ID: {}", fields.size(), templateGroupId);

            for (TemplateField field : fields) {
                field.setHidden(hidden);
            }

            template.setHidden(hidden);

            templateRepository.save(template);
            templateFieldRepository.saveAll(fields);
            logger.debug("Template and {} fields updated with hidden: {}", fields.size(), hidden);
        } catch (Exception e) {
            logger.error("Error in triggerTemplate for group ID {}: {}", templateGroupId, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteTemplateField(Long fieldId) {
        logger.info("Deleting template field with ID: {}", fieldId);
        try {
            triggerTemplateField(fieldId, true);
            logger.info("Template field with ID: {} deleted successfully", fieldId);
        } catch (Exception e) {
            logger.error("Error deleting template field with ID {}: {}", fieldId, e.getMessage(), e);
            throw e;
        }
    }

    public void restoreTemplateField(Long fieldId) {
        logger.info("Restoring template field with ID: {}", fieldId);
        try {
            triggerTemplateField(fieldId, false);
            logger.info("Template field with ID: {} restored successfully", fieldId);
        } catch (Exception e) {
            logger.error("Error restoring template field with ID {}: {}", fieldId, e.getMessage(), e);
            throw e;
        }
    }

    private void triggerTemplateField(Long fieldId, boolean hidden) {
        logger.debug("Triggering template field with ID: {} to hidden: {}", fieldId, hidden);
        try {
            templateFieldRepository.findById(fieldId).ifPresentOrElse(field -> {
                field.setHidden(hidden);
                templateFieldRepository.save(field);
                logger.debug("Template field with ID: {} updated with hidden: {}", fieldId, hidden);
            }, () -> {
                logger.error("Template field not found with ID: {}", fieldId);
                throw new EntityNotFoundException("Template field not found with ID: " + fieldId);
            });
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error in triggerTemplateField for field ID {}: {}", fieldId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean updateTemplate(String groupId, UpdateTemplateWithFieldsRequestDTO request) {
        logger.info("Updating template with group ID: {}", groupId);
        try {
            Optional<Template> optionalTemplate = templateRepository.findByGroupId(groupId);

            if (optionalTemplate.isEmpty()) {
                logger.warn("Template not found with group ID: {}", groupId);
                return false;
            }

            // Update base template details
            Template existingTemplate = updateTemplateDetails(optionalTemplate.get(), request);
            templateRepository.save(existingTemplate);
            logger.debug("Template details updated for group ID: {}", groupId);

            List<TemplateField> existingFields = templateFieldRepository.findByGroupId(groupId);

                        Set<Long> incomingFieldIds = request.getFields().stream()
                                .map(TemplateFieldDTO::getId)
                                .filter(Objects::nonNull)
                                .map(Long::valueOf)
                                .collect(Collectors.toSet());

                        List<TemplateField> fieldsToDelete = existingFields.stream()
                                .filter(f -> !incomingFieldIds.contains(f.getId()))
                                .collect(Collectors.toList());


            if (!fieldsToDelete.isEmpty()) {
                for (TemplateField field : fieldsToDelete) {
                    deleteTemplateField(field.getId());
                }
                logger.debug("Deleted {} fields for group ID: {}", fieldsToDelete.size(), groupId);
            }

            // Save or update incoming fields
            int fieldCount = 0;
            for (TemplateFieldDTO incomingField : request.getFields()) {
                TemplateField field = mapToEntity(incomingField, groupId);
                templateFieldRepository.save(field); // Insert or update
                fieldCount++;
            }

            logger.info("Template with group ID: {} updated successfully with {} fields", groupId, fieldCount);
            return true;
        } catch (Exception e) {
            logger.error("Error updating template with group ID {}: {}", groupId, e.getMessage(), e);
            throw e;
        }
    }


    private Template updateTemplateDetails(Template template, UpdateTemplateWithFieldsRequestDTO request) {
        logger.debug("Updating template details for template ID: {}", template.getId());
        try {
            template.setEnglishName(request.getEnglishName());
            template.setArabicName(request.getArabicName());
            template.setEnglishDescription(request.getEnglishDescription());
            template.setArabicDescription(request.getArabicDescription());
            logger.debug("Template details updated successfully for template ID: {}", template.getId());
            return template;
        } catch (Exception e) {
            logger.error("Error updating template details for template ID {}: {}", template.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public Response<String> createTemplateWithFields(CreateTemplateWithFieldsRequestDTO request) {
        logger.info("Creating new template with fields. Template name: {}", request.getEnglishName());
        try {
            // Save the template
            Template template = new Template();
            template.setEnglishName(request.getEnglishName());
            template.setArabicName(request.getArabicName());
            template.setEnglishDescription(request.getEnglishDescription());
            template.setArabicDescription(request.getArabicDescription());
            template.setGroupId(UUID.randomUUID().toString());

            logger.debug("Saving template with name: {}", request.getEnglishName());
            Template savedTemplate = templateRepository.save(template);
            logger.debug("Template saved with ID: {} and group ID: {}", savedTemplate.getId(), savedTemplate.getGroupId());

            int fieldCount = 0;
            for (TemplateFieldDTO dto : request.getFields()) {
                String controlTypeCode = dto.getControlType();
                if (controlTypeCode == null || controlTypeCode.isBlank()) {
                    logger.error("ControlType code is required for field: {}", dto.getLabelEn());
                    throw new RuntimeException("ControlType code is required for all fields.");
                }

                logger.debug("Finding control type with code: {}", controlTypeCode);
                ControlTypeLookup controlType = controlTypeLookupRepository.findByCode(controlTypeCode)
                        .orElseThrow(() -> {
                            logger.error("ControlType with code [{}] not found", controlTypeCode);
                            return new RuntimeException("ControlType with code [" + controlTypeCode + "] not found");
                        });

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

                logger.debug("Saving template field: {}", field.getLabelEn());
                templateFieldRepository.save(field);
                fieldCount++;
            }

            logger.info("Template created successfully with name: {}, group ID: {}, and {} fields", 
                    savedTemplate.getEnglishName(), savedTemplate.getGroupId(), fieldCount);
            return ResponseUtils.success(HttpStatus.CREATED.value(), "Template and fields created successfully with group id "+savedTemplate.getGroupId());

        } catch (Exception e) {
            logger.error("Failed to create template: {}", e.getMessage(), e);
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create template: " + e.getMessage());
        }
    }

    public Optional<List<ControlTypeLookup>> getControlTypes() {
        logger.info("Fetching all control types");
        try {
            List<ControlTypeLookup> controlTypes = controlTypeLookupRepository.findAll();
            logger.info("Retrieved {} control types", controlTypes.size());
            return Optional.of(controlTypes);
        } catch (Exception e) {
            logger.error("Error fetching control types: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Page<ServiceResponseDTO> getAllServiceDTOs(Pageable pageable) {
        logger.info("Fetching all service DTOs with pagination");
        try {
            Page<Services> servicesPage = serviceRepository.findAll(pageable);
            Page<ServiceResponseDTO> dtoPage = servicesPage.map(this::mapToDTO);
            logger.info("Retrieved {} service DTOs", dtoPage.getTotalElements());
            return dtoPage;
        } catch (Exception e) {
            logger.error("Error fetching all service DTOs: {}", e.getMessage(), e);
            throw e;
        }
    }

    private ServiceResponseDTO mapToDTO(Services service) {
        logger.debug("Mapping service with ID: {} to DTO", service.getId());
        try {
            Template template = service.getTemplate();
            ServiceType serviceType = service.getServiceType();
            ServiceStatus status = service.getStatus();

            ServiceResponseDTO dto = new ServiceResponseDTO(
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
            logger.debug("Service with ID: {} mapped to DTO successfully", service.getId());
            return dto;
        } catch (Exception e) {
            logger.error("Error mapping service with ID {} to DTO: {}", service.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private TemplateField mapToEntity(TemplateFieldDTO dto, String groupId) {
        logger.debug("Mapping TemplateFieldDTO to entity for field: {} in group: {}", dto.getLabelEn(), groupId);
        try {
            ControlTypeLookup controlType = controlTypeLookupRepository.findByCode(dto.getControlType())
                    .orElseThrow(() -> {
                        logger.error("Invalid control type: {}", dto.getControlType());
                        return new RuntimeException("Invalid control type: " + dto.getControlType());
                    });

            TemplateField field = new TemplateField();

            field.setId(dto.getId()); // Will trigger update if ID is present
            field.setLabelEn(dto.getLabelEn());
            field.setLabelAr(dto.getLabelAr());
            field.setControlType(controlType);
            field.setRequired(dto.isRequired());
            field.setAttachment(dto.isAttachment());
            field.setSorting(dto.getSorting());
            field.setPlaceholderEn(dto.getPlaceholderEn());
            field.setPlaceholderAr(dto.getPlaceholderAr());
            field.setExtraDataEn(convertListToString(dto.getExtraDataEn()));
            field.setExtraDataAr(convertListToString(dto.getExtraDataAr()));
            field.setGroupId(groupId);

            logger.debug("TemplateFieldDTO mapped to entity successfully for field: {}", dto.getLabelEn());
            return field;
        } catch (Exception e) {
            logger.error("Error mapping TemplateFieldDTO to entity for field {}: {}", dto.getLabelEn(), e.getMessage(), e);
            throw e;
        }
    }



    public Optional<RequestDetailsDTO> getRequestDetails(String requestNumber) {
        logger.info("Fetching request details for request number: {}", requestNumber);
        try {
            Optional<RequestDetailsDTO> result = requestRepository.findById(requestNumber)
                    .map(request -> {
                        logger.debug("Found request with number: {}, fetching field values", requestNumber);
                        List<TemplateFieldValue> fieldValues = templateFieldValueRepository
                                .findByCustomerNumber(request.getCustomerNumber());
                        logger.debug("Retrieved {} field values for customer number: {}", fieldValues.size(), request.getCustomerNumber());

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

                        logger.debug("Mapped {} field values to DTOs", fields.size());

                        RequestDetailsDTO dto = new RequestDetailsDTO(
                                request.getRequestNumber(),
                                request.getDescription(),
                                request.getRequestedBy(),
                                request.getCustomerNumber(),
                                request.getStatusUpdatedBy(),
                                request.getService().getId(),
                                request.getStatus().name(),
                                request.getTime(),
                                request.getSlaTime(),
                                request.getComment(),
                                fields
                        );

                        logger.info("Request details retrieved successfully for request number: {}", requestNumber);
                        return dto;
                    });

            if (result.isEmpty()) {
                logger.warn("No request found with request number: {}", requestNumber);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error fetching request details for request number {}: {}", requestNumber, e.getMessage(), e);
            throw e;
        }
    }

    private List<String> convertStringToList(String value) {
        logger.debug("Converting string to list: {}", value);
        try {
            if (value == null || value.isBlank()) {
                logger.debug("Empty or null string, returning empty list");
                return Collections.emptyList();
            }
            List<String> result = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            logger.debug("Converted string to list with {} items", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error converting string to list: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String convertListToString(List<String> list) {
        logger.debug("Converting list to string, list size: {}", list != null ? list.size() : 0);
        try {
            String result = (list == null || list.isEmpty()) ? null : String.join(",", list);
            logger.debug("Converted list to string: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error converting list to string: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ServiceResponseDTO getService(Long serviceId) {
        logger.info("Fetching service with ID: {}", serviceId);
        try {
            Services service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> {
                        logger.error("Service not found with ID: {}", serviceId);
                        return new EntityNotFoundException("Service not found with ID: " + serviceId);
                    });
            ServiceResponseDTO dto = mapToDTO(service);
            logger.info("Service with ID: {} fetched successfully", serviceId);
            return dto;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching service with ID {}: {}", serviceId, e.getMessage(), e);
            throw e;
        }
    }
}
