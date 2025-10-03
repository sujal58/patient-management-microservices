package org.pm.billingservice.repository;

import org.pm.billingservice.model.BillingAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingAccountRepository extends JpaRepository<BillingAccount, String> {
    boolean existsByPatientId(String patientId);
    Optional<BillingAccount> findByPatientId(String patientId);
    Page<BillingAccount> findAll(Pageable pageable);
}
