package org.pm.billingservice.repository;

import org.pm.billingservice.model.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, String> {
    boolean existsByPatientId(String patientId);
    Optional<BillingAccount> findByPatientId(String patientId);
}
