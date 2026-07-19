package org.pm.patientservice.grpc;

import billing.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.pm.patientservice.exception.BillingServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
public class BillingServiceGrpcClient {
    private static final Logger logger = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub billingServiceBlockingStub;
    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort

    ) {
        logger.info("Connecting to billing service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();

        billingServiceBlockingStub = billing.BillingServiceGrpc.newBlockingStub(channel);

    }


    public BillingResponse createBillingAccount(String patientId, String name, String email){
        try {
            BillingRequest request = BillingRequest.newBuilder()
                    .setPatientId(patientId)
                    .setName(name)
                    .setEmail(email)
                    .build();

            BillingResponse response = billingServiceBlockingStub.createBillingAccount(request);
            logger.info("Successfully created billing account for patient ID: {}", patientId);
            return response;
        } catch (StatusRuntimeException e) {
            logger.error("Failed to create billing account for patient ID: {}. Error: {}", patientId, e.getStatus(), e);
            throw new BillingServiceException("Failed to create billing account: " + e.getStatus().getDescription());
        }
    }

    public void deactivateBillingAccount(String patientId) {
        logger.info("Deactivating billing account for patient ID: {}", patientId);

        // Validate input
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }

        try {
            DeactivateBillingRequest request = DeactivateBillingRequest.newBuilder()
                    .setPatientId(patientId)
                    .build();

            billingServiceBlockingStub.deactivateBillingAccount(request);
            logger.info("Successfully deactivated billing account for patient ID: {}", patientId);
        } catch (StatusRuntimeException e) {
            logger.error("Failed to deactivate billing account for patient ID: {}. Error: {}", patientId, e.getStatus(), e);
            throw new BillingServiceException("Failed to deactivate billing account: " + e.getStatus().getDescription());
        }
    }

    public BillResponse addCharge(String patientId, double amount, String description, LocalDate chargeDate) {
        logger.info("Adding charge for patient ID: {}, date: {}", patientId, chargeDate);

        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (chargeDate == null) {
            throw new IllegalArgumentException("Charge date cannot be null");
        }

        try {
            AddChargeRequest request = AddChargeRequest.newBuilder()
                    .setPatientId(patientId)
                    .setAmount(amount)
                    .setDescription(description)
                    .setChargeDate(chargeDate.toString())
                    .build();

            BillResponse response = billingServiceBlockingStub.addCharge(request);
            logger.info("Successfully added charge for patient ID: {}, date: {}", patientId, chargeDate);
            return response;
        } catch (StatusRuntimeException e) {
            logger.error("Failed to add charge for patient ID: {}, date: {}. Error: {}", patientId, chargeDate, e.getStatus(), e);
            throw new BillingServiceException("Failed to add charge: " + e.getStatus().getDescription());
        }
    }


    public BillsResponse getBillsByPatient(String patientId, int page, int size) {
        logger.info("Fetching bills for patient ID: {}, page: {}, size: {}", patientId, page, size);

        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        try {
            GetBillsByPatientRequest request = GetBillsByPatientRequest.newBuilder()
                    .setPatientId(patientId)
                    .setPage(page)
                    .setSize(size)
                    .build();

            BillsResponse response = billingServiceBlockingStub.getBillsByPatient(request);
            logger.info("Successfully fetched bills for patient ID: {}", patientId);
            return response;
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get bills for patient ID: {}. Error: {}", patientId, e.getStatus(), e);
            throw new BillingServiceException("Failed to get bills: " + e.getStatus().getDescription());
        }
    }


    public BillsResponse getBillAccountByPatient(String patientId, int page, int size) {
        logger.info("Fetching bills for patient ID: {}, page: {}, size: {}", patientId, page, size);

        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        try {
            GetBillsByPatientRequest request = GetBillsByPatientRequest.newBuilder()
                    .setPatientId(patientId)
                    .setPage(page)
                    .setSize(size)
                    .build();

            BillsResponse response = billingServiceBlockingStub.getBillsByPatient(request);
            logger.info("Successfully fetched bills for patient ID: {}", patientId);
            return response;
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get bills for patient ID: {}. Error: {}", patientId, e.getStatus(), e);
            throw new BillingServiceException("Failed to get bills: " + e.getStatus().getDescription());
        }
    }

    public BillResponse getBillByPatientAndDate(String patientId, LocalDate billDate) {
        logger.info("Fetching bill for patient ID: {}, date: {}", patientId, billDate);

        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (billDate == null) {
            throw new IllegalArgumentException("Bill date cannot be null");
        }

        try {
            GetBillByPatientAndDateRequest request = GetBillByPatientAndDateRequest.newBuilder()
                    .setPatientId(patientId)
                    .setBillDate(billDate.toString())
                    .build();

            BillResponse response = billingServiceBlockingStub.getBillByPatientAndDate(request);
            logger.info("Successfully fetched bill for patient ID: {}, date: {}", patientId, billDate);
            return response;
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get bill for patient ID: {}, date: {}. Error: {}", patientId, billDate, e.getStatus(), e);
            throw new BillingServiceException("Failed to get bill: " + e.getStatus().getDescription());
        }
    }

}
