package com.ABIC.CustomerRequest.web.serviceManagment.service;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import com.ABIC.CustomerRequest.mobile.requestManagmentService.repository.RequestRepository;
import com.ABIC.CustomerRequest.util.Response;
import com.ABIC.CustomerRequest.web.serviceManagment.model.*;
import com.ABIC.CustomerRequest.web.serviceManagment.model.dto.*;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServiceManagementServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private ServiceStatusRepository serviceStatusRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFieldRepository templateFieldRepository;

    @Mock
    private TemplateFieldValueRepository templateFieldValueRepository;

    @Mock
    private ControlTypeLookupRepository controlTypeLookupRepository;

    @InjectMocks
    private ServiceManagementService serviceManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateService() {
        // Arrange
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setEnglishName("Test Service");
        serviceDTO.setArabicName("خدمة تجريبية");
        serviceDTO.setDescription("Service Description");
        serviceDTO.setArabicDescription("وصف الخدمة");
        serviceDTO.setSlaTime(5);
        serviceDTO.setStatusId(1L);
        serviceDTO.setServiceTypeId(1L);
        serviceDTO.setTemplateId(1L);

        ServiceType mockServiceType = new ServiceType();
        mockServiceType.setId(1L);

        ServiceStatus mockStatus = new ServiceStatus();
        mockStatus.setId(1L);

        Template mockTemplate = new Template();
        mockTemplate.setId(1L);

        when(serviceTypeRepository.findById(1L)).thenReturn(Optional.of(mockServiceType));
        when(serviceStatusRepository.findById(1L)).thenReturn(Optional.of(mockStatus));
        when(templateRepository.findById(1L)).thenReturn(Optional.of(mockTemplate));

        // Act
        serviceManagementService.createService(serviceDTO);

        // Assert
        verify(serviceRepository, times(1)).save(any(Services.class));
    }

    @Test
    void testCreateService_ThrowsExceptionWhenServiceTypeNotFound() {
        // Arrange
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setServiceTypeId(999L);

        when(serviceTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            serviceManagementService.createService(serviceDTO);
        });

        assertEquals("Service Type not found", exception.getMessage());
        verify(serviceRepository, never()).save(any(Services.class));
    }

    @Test
    void testUpdateService() {
        // Arrange
        Long serviceId = 1L;
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setEnglishName("Updated Service");
        serviceDTO.setArabicName("خدمة محدثة");
        serviceDTO.setDescription("Updated Description");
        serviceDTO.setArabicDescription("وصف محدث");
        serviceDTO.setSlaTime(10);
        serviceDTO.setStatusId(2L);
        serviceDTO.setServiceTypeId(2L);
        serviceDTO.setTemplateId(2L);

        Services existingService = new Services();
        existingService.setId(serviceId);

        ServiceType mockServiceType = new ServiceType();
        mockServiceType.setId(2L);

        ServiceStatus mockStatus = new ServiceStatus();
        mockStatus.setId(2L);

        Template mockTemplate = new Template();
        mockTemplate.setId(2L);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(existingService));
        when(serviceTypeRepository.findById(2L)).thenReturn(Optional.of(mockServiceType));
        when(serviceStatusRepository.findById(2L)).thenReturn(Optional.of(mockStatus));
        when(templateRepository.findById(2L)).thenReturn(Optional.of(mockTemplate));

        // Act
        serviceManagementService.updateService(serviceId, serviceDTO);

        // Assert
        verify(serviceRepository, times(1)).save(existingService);
        assertEquals("Updated Service", existingService.getEnglishName());
        assertEquals("خدمة محدثة", existingService.getArabicName());
        assertEquals("Updated Description", existingService.getDescription());
        assertEquals("وصف محدث", existingService.getArabicDescription());
        assertEquals(10, existingService.getSlaTime());
        assertEquals(mockStatus, existingService.getStatus());
        assertEquals(mockServiceType, existingService.getServiceType());
        assertEquals(mockTemplate, existingService.getTemplate());
    }

    @Test
    void testGetAllServices() {
        // Arrange
        List<Services> servicesList = new ArrayList<>();
        Services service1 = new Services();
        service1.setId(1L);
        service1.setEnglishName("Service 1");
        servicesList.add(service1);

        Services service2 = new Services();
        service2.setId(2L);
        service2.setEnglishName("Service 2");
        servicesList.add(service2);

        Page<Services> servicesPage = new PageImpl<>(servicesList);
        Pageable pageable = mock(Pageable.class);

        when(serviceRepository.findAll(pageable)).thenReturn(servicesPage);

        // Act
        Page<Services> result = serviceManagementService.getAllServices(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("Service 1", result.getContent().get(0).getEnglishName());
        assertEquals("Service 2", result.getContent().get(1).getEnglishName());
    }

    @Test
    void testToggleServiceVisibility() {
        // Arrange
        Long serviceId = 1L;
        Long statusId = 2L;

        Services service = new Services();
        service.setId(serviceId);

        ServiceStatus status = new ServiceStatus();
        status.setId(statusId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceStatusRepository.findById(statusId)).thenReturn(Optional.of(status));

        // Act
        serviceManagementService.toggleServiceVisibility(serviceId, statusId);

        // Assert
        verify(serviceRepository, times(1)).save(service);
        assertEquals(status, service.getStatus());
    }

    @Test
    void testGetAllTemplates() {
        // Arrange
        List<Template> templates = new ArrayList<>();
        Template template1 = new Template();
        template1.setId(1L);
        template1.setEnglishName("Template 1");
        templates.add(template1);

        Template template2 = new Template();
        template2.setId(2L);
        template2.setEnglishName("Template 2");
        templates.add(template2);

        when(templateRepository.findAllByHiddenIsFalse()).thenReturn(templates);

        // Act
        List<Template> result = serviceManagementService.getAllTemplates();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Template 1", result.get(0).getEnglishName());
        assertEquals("Template 2", result.get(1).getEnglishName());
    }

    @Test
    void testCreateTemplateWithFields() {
        // Arrange
        CreateTemplateWithFieldsRequestDTO requestDTO = new CreateTemplateWithFieldsRequestDTO();
        requestDTO.setEnglishName("Test Template");
        requestDTO.setArabicName("قالب اختبار");
        requestDTO.setEnglishDescription("Test Description");
        requestDTO.setArabicDescription("وصف الاختبار");

        List<TemplateFieldDTO> fields = new ArrayList<>();
        TemplateFieldDTO field = new TemplateFieldDTO();
        field.setLabelEn("Field 1");
        field.setLabelAr("حقل 1");
        field.setControlType("text");
        field.setRequired(true);
        fields.add(field);
        requestDTO.setFields(fields);

        ControlTypeLookup controlType = new ControlTypeLookup();
        controlType.setCode("text");

        when(templateRepository.save(any(Template.class))).thenAnswer(invocation -> {
            Template template = invocation.getArgument(0);
            template.setId(1L);
            return template;
        });

        when(controlTypeLookupRepository.findByCode("text")).thenReturn(Optional.of(controlType));

        // Act
        Response<String> response = serviceManagementService.createTemplateWithFields(requestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertTrue(response.getData().contains("Template and fields created successfully"));
        verify(templateRepository, times(1)).save(any(Template.class));
        verify(templateFieldRepository, times(1)).save(any(TemplateField.class));
    }

    @Test
    void testGetRequestDetails() {
        // Arrange
        String requestNumber = "REQ-123";
        Request request = new Request();
        request.setRequestNumber(requestNumber);
        request.setDescription("Test Request");
        request.setCustomerNumber("CUST-001");

        Services service = new Services();
        service.setId(1L);
        request.setService(service);

        request.setStatus(Request.Status.PENDING);

        TemplateField field = new TemplateField();
        field.setId(1L);
        field.setLabelEn("Field 1");
        field.setLabelAr("حقل 1");

        TemplateFieldValue fieldValue = new TemplateFieldValue();
        fieldValue.setField(field);
        fieldValue.setValue("Test Value");

        List<TemplateFieldValue> fieldValues = List.of(fieldValue);

        when(requestRepository.findById(requestNumber)).thenReturn(Optional.of(request));
        when(templateFieldValueRepository.findByCustomerNumber(request.getCustomerNumber())).thenReturn(fieldValues);

        // Act
        Optional<RequestDetailsDTO> result = serviceManagementService.getRequestDetails(requestNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(requestNumber, result.get().getRequestNumber());
        assertEquals("Test Request", result.get().getDescription());
        assertEquals("CUST-001", result.get().getCustomerNumber());
        assertEquals(1, result.get().getFields().size());
        assertEquals("Field 1", result.get().getFields().get(0).getLabelEn());
        assertEquals("Test Value", result.get().getFields().get(0).getValue());
    }

    @Test
    void testDeleteTemplate() {
        // Arrange
        String groupId = "template-group-1";
        Template template = new Template();
        template.setGroupId(groupId);

        List<TemplateField> fields = new ArrayList<>();
        TemplateField field1 = new TemplateField();
        field1.setId(1L);
        fields.add(field1);

        when(templateRepository.findByGroupId(groupId)).thenReturn(Optional.of(template));
        when(templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId)).thenReturn(fields);

        // Act
        serviceManagementService.deleteTemplate(groupId);

        // Assert
        verify(templateRepository, times(1)).save(template);
        verify(templateFieldRepository, times(1)).saveAll(fields);
        assertTrue(template.isHidden());
        assertTrue(fields.get(0).isHidden());
    }

    @Test
    void testDeleteTemplate_ThrowsExceptionWhenTemplateNotFound() {
        // Arrange
        String groupId = "non-existent-group";
        when(templateRepository.findByGroupId(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            serviceManagementService.deleteTemplate(groupId);
        });
    }

    @Test
    void testRestoreTemplate() {
        // Arrange
        String groupId = "template-group-1";
        Template template = new Template();
        template.setGroupId(groupId);
        template.setHidden(true);

        List<TemplateField> fields = new ArrayList<>();
        TemplateField field1 = new TemplateField();
        field1.setId(1L);
        field1.setHidden(true);
        fields.add(field1);

        when(templateRepository.findByGroupId(groupId)).thenReturn(Optional.of(template));
        when(templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId)).thenReturn(fields);

        // Act
        serviceManagementService.restoreTemplate(groupId);

        // Assert
        verify(templateRepository, times(1)).save(template);
        verify(templateFieldRepository, times(1)).saveAll(fields);
        assertFalse(template.isHidden());
        assertFalse(fields.get(0).isHidden());
    }

    @Test
    void testUpdateTemplate() {
        // Arrange
        String groupId = "template-group-1";
        UpdateTemplateWithFieldsRequestDTO requestDTO = new UpdateTemplateWithFieldsRequestDTO();
        requestDTO.setEnglishName("Updated Template");
        requestDTO.setArabicName("قالب محدث");
        requestDTO.setEnglishDescription("Updated Description");
        requestDTO.setArabicDescription("وصف محدث");

        Template existingTemplate = new Template();
        existingTemplate.setGroupId(groupId);

        // Create an existing field to update
        TemplateField existingField = new TemplateField();
        existingField.setId(1L);
        existingField.setLabelEn("Old Label");
        existingField.setGroupId(groupId);

        // Create a field DTO for updating
        TemplateFieldDTO fieldDTO = new TemplateFieldDTO();
        fieldDTO.setId(1L);
        fieldDTO.setLabelEn("Updated Label");
        fieldDTO.setLabelAr("تسمية محدثة");
        fieldDTO.setControlType("text");
        fieldDTO.setRequired(true);

        // Create a new field DTO to add
        TemplateFieldDTO newFieldDTO = new TemplateFieldDTO();
        newFieldDTO.setLabelEn("New Field");
        newFieldDTO.setLabelAr("حقل جديد");
        newFieldDTO.setControlType("dropdown");
        newFieldDTO.setRequired(false);

        List<TemplateFieldDTO> fieldDTOs = List.of(fieldDTO, newFieldDTO);
        requestDTO.setFields(fieldDTOs);

        ControlTypeLookup textControlType = new ControlTypeLookup();
        textControlType.setCode("text");

        ControlTypeLookup dropdownControlType = new ControlTypeLookup();
        dropdownControlType.setCode("dropdown");

        when(templateRepository.findByGroupId(groupId)).thenReturn(Optional.of(existingTemplate));
        when(templateFieldRepository.findByGroupId(groupId)).thenReturn(List.of(existingField));
        when(controlTypeLookupRepository.findByCode("text")).thenReturn(Optional.of(textControlType));
        when(controlTypeLookupRepository.findByCode("dropdown")).thenReturn(Optional.of(dropdownControlType));

        // Act
        boolean result = serviceManagementService.updateTemplate(groupId, requestDTO);

        // Assert
        assertTrue(result);
        verify(templateRepository, times(1)).save(existingTemplate);
        assertEquals("Updated Template", existingTemplate.getEnglishName());
        assertEquals("قالب محدث", existingTemplate.getArabicName());

        // Verify existing field was updated
        verify(templateFieldRepository, times(1)).save(existingField);

        // Verify new field was created
        verify(templateFieldRepository, times(1)).save(argThat(field -> 
            field.getLabelEn().equals("New Field") && field.getGroupId().equals(groupId)
        ));
    }

    @Test
    void testUpdateTemplate_TemplateNotFound() {
        // Arrange
        String groupId = "non-existent-group";
        UpdateTemplateWithFieldsRequestDTO requestDTO = new UpdateTemplateWithFieldsRequestDTO();

        when(templateRepository.findByGroupId(groupId)).thenReturn(Optional.empty());

        // Act
        boolean result = serviceManagementService.updateTemplate(groupId, requestDTO);

        // Assert
        assertFalse(result);
        verify(templateRepository, never()).save(any());
        verify(templateFieldRepository, never()).save(any());
    }

    @Test
    void testGetTemplateFieldsByGroupId() {
        // Arrange
        String groupId = "template-group-1";

        TemplateField field1 = new TemplateField();
        field1.setId(1L);
        field1.setLabelEn("Field 1");
        field1.setLabelAr("حقل 1");
        field1.setRequired(true);
        field1.setSorting(1);

        ControlTypeLookup controlType = new ControlTypeLookup();
        controlType.setCode("text");
        field1.setControlType(controlType);

        List<TemplateField> fields = List.of(field1);

        when(templateFieldRepository.findByGroupIdAndHiddenIsFalse(groupId)).thenReturn(fields);

        // Act
        List<TemplateFieldDTO> result = serviceManagementService.getTemplateFieldsByGroupId(groupId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Field 1", result.get(0).getLabelEn());
        assertEquals("حقل 1", result.get(0).getLabelAr());
        assertEquals("text", result.get(0).getControlType());
        assertTrue(result.get(0).isRequired());
    }

    @Test
    void testGetSubmissionsByCustomerNumber() {
        // Arrange
        String customerNumber = "CUST-001";

        TemplateFieldValue value1 = new TemplateFieldValue();
        value1.setId(1L);
        value1.setValue("Value 1");
        value1.setCustomerNumber(customerNumber);

        List<TemplateFieldValue> values = List.of(value1);
        Page<TemplateFieldValue> page = new PageImpl<>(values);
        Pageable pageable = mock(Pageable.class);

        when(templateFieldValueRepository.getAllByCustomerNumber(customerNumber, pageable)).thenReturn(page);

        // Act
        Page<TemplateFieldValue> result = serviceManagementService.getSubmissionsByCustomerNumber(customerNumber, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("Value 1", result.getContent().get(0).getValue());
        assertEquals(customerNumber, result.getContent().get(0).getCustomerNumber());
    }

    @Test
    void testGetAllServiceDTOs() {
        // Arrange
        Services service1 = new Services();
        service1.setId(1L);
        service1.setEnglishName("Service 1");
        service1.setArabicName("خدمة 1");

        Template template = new Template();
        template.setId(1L);
        template.setGroupId("group-1");
        template.setEnglishName("Template 1");
        template.setArabicName("قالب 1");
        service1.setTemplate(template);

        ServiceType serviceType = new ServiceType();
        serviceType.setId(1L);
        serviceType.setTypeEn("Type 1");
        serviceType.setTypeAr("نوع 1");
        service1.setServiceType(serviceType);

        ServiceStatus status = new ServiceStatus();
        status.setId(1L);
        status.setStatusEn("Active");
        status.setStatusAr("نشط");
        service1.setStatus(status);

        service1.setSlaTime(5);

        List<Services> services = List.of(service1);
        Page<Services> page = new PageImpl<>(services);
        Pageable pageable = mock(Pageable.class);

        when(serviceRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<ServiceResponseDTO> result = serviceManagementService.getAllServiceDTOs(pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        ServiceResponseDTO dto = result.getContent().get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Service 1", dto.getEnglishName());
        assertEquals("خدمة 1", dto.getArabicName());
        assertEquals("Template 1", dto.getTemplateNameEn());
        assertEquals("قالب 1", dto.getTemplateNameAr());
        assertEquals("Type 1", dto.getServiceTypeNameEn());
        assertEquals("نوع 1", dto.getServiceTypeNameAr());
        assertEquals("Active", dto.getStatusEn());
        assertEquals("نشط", dto.getStatusAr());
        assertEquals(5, dto.getSlaTime());
    }
}
