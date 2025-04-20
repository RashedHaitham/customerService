package com.ABIC.CustomerRequest.mobile.requestManagmentService.repository;

import com.ABIC.CustomerRequest.mobile.requestManagmentService.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, String> {
    Request findByRequestNumber(String requestNumber);

    @Query("SELECT r.status, COUNT(r) FROM Request r GROUP BY r.status")
    List<Object[]> countRequestsByStatus();

    Page<Request> findByStatus(Request.Status status, Pageable pageable);

    Page<Request> findRequestByCustomerNumber(String requestNumber, Pageable pageable);

    @Query(value = "SELECT COALESCE(MAX(sequence_id), 99999) + 1 FROM requests", nativeQuery = true)
    Long getNextSequenceId();
}