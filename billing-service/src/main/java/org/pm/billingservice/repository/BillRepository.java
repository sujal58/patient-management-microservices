package org.pm.billingservice.repository;

import org.pm.billingservice.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, String> {
    Optional<Bill> findByPatientIdAndBillDate(String patientId, LocalDate billDate);
    Page<Bill> findByPatientId(String patientId, Pageable pageable);
}
