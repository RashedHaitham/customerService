package com.ABIC.CustomerRequest.web.serviceManagment.service;

import com.ABIC.CustomerRequest.web.serviceManagment.model.ControlTypeLookup;
import com.ABIC.CustomerRequest.web.serviceManagment.model.ServiceStatus;
import com.ABIC.CustomerRequest.web.serviceManagment.model.ServiceType;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.ControlTypeLookupRepository;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.ServiceStatusRepository;
import com.ABIC.CustomerRequest.web.serviceManagment.repository.ServiceTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ControlTypeInitializer {

    private final ControlTypeLookupRepository repository;
    private final ServiceStatusRepository serviceStatusRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    @PostConstruct
    public void init() {
        initControlTypes();
        initServiceStatuses();
        initServiceTypes();
    }

    private void initControlTypes() {
        if (repository.count() == 0) {
            List<ControlTypeLookup> defaultTypes = List.of(
                    new ControlTypeLookup(null, "TEXTBOX", "Text Box", "مربع نص"),
                    new ControlTypeLookup(null, "RADIOBUTTON", "Radio Button", "زر اختياري"),
                    new ControlTypeLookup(null, "DROPDOWN", "Dropdown", "قائمة منسدلة"),
                    new ControlTypeLookup(null, "CHECKBOX", "Checkbox", "مربع اختيار"),
                    new ControlTypeLookup(null, "DATEPICKER", "Date Picker", "منتقي التاريخ"),
                    new ControlTypeLookup(null, "TEXTAREA", "Text Area", "منطقة نص"),
                    new ControlTypeLookup(null, "NUMBER", "Number Input", "إدخال رقمي"),
                    new ControlTypeLookup(null, "ATTACHMENT", "Attachment", "إرفاق")
            );
            repository.saveAll(defaultTypes);
            System.out.println("Control types initialized.");
        }
    }

    private void initServiceStatuses() {
        if (serviceStatusRepository.count() == 0) {
            var statuses = List.of(
                    new ServiceStatus(null, "Active", "نشط"),
                    new ServiceStatus(null, "Disabled", "غير نشط")
            );
            serviceStatusRepository.saveAll(statuses);
            System.out.println("Service statuses initialized.");
        }
    }

    private void initServiceTypes() {
        if (serviceTypeRepository.count() == 0) {
            var types = List.of(
                    new ServiceType(null, "IT Support", "الدعم الفني", "Helpdesk Requests", "طلبات الدعم"),
                    new ServiceType(null, "HR Services", "خدمات الموارد البشرية", "Leave and Payroll", "الإجازات والرواتب"),
                    new ServiceType(null, "Finance", "المالية", "Payment and Billing", "الدفع والفواتير")
            );
            serviceTypeRepository.saveAll(types);
            System.out.println("Service types initialized.");
        }
    }

}
