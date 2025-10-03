package org.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.DeactivateBillingRequest;
import billing.DeactivateBillingResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import net.devh.boot.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import org.pm.billingservice.model.BillingAccount;
import org.pm.billingservice.repository.BillingAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BillingGrpcService.class);

    private final BillingAccountRepository billingAccountRepository;

    public BillingGrpcService(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    @Override
    @Transactional
    public void createBillingAccount(BillingRequest request, StreamObserver<BillingResponse> responseObserver) {
        logger.info("Received createBillingAccount request for patient ID: {}", request.getPatientId());

        try {
            // Validate request
//            validateBillingRequest(request);

            // Check for existing account
            if (billingAccountRepository.existsByPatientId(request.getPatientId())) {
                throw new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription(
                        "Billing account already exists for patient ID: " + request.getPatientId()));
            }

            // Create and save billing account
            BillingAccount account = new BillingAccount();
            account.setPatientId(request.getPatientId());
            account.setName(request.getName());
            account.setEmail(request.getEmail());
            account.setStatus("Active");
            account.setBillingAccountId(generateBillingAccountId()); // Generate unique ID
            account = billingAccountRepository.save(account);

            // Publish Kafka event
//            publishBillingEvent("BILLING_ACCOUNT_CREATED", account);

            // Build and send response
            BillingResponse response = BillingResponse.newBuilder()
                    .setAccountId(account.getBillingAccountId())
                    .setPatientId(account.getPatientId())
                    .setStatus(account.getStatus())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to create billing account for patient ID: {}. Error: {}", request.getPatientId(), e.getStatus());
            responseObserver.onError(e);
        } catch (Exception e) {
            logger.error("Unexpected error creating billing account for patient ID: {}", request.getPatientId(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException());
        }
    }


    @Override
    @Transactional
    public void deactivateBillingAccount(DeactivateBillingRequest request, StreamObserver<DeactivateBillingResponse> responseObserver) {
        logger.info("Received deactivateBillingAccount request for patient ID: {}", request.getPatientId());

        try {
            // Validate request
            if (request.getPatientId().isEmpty()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
            }

            // Find and deactivate account
            BillingAccount account = billingAccountRepository.findByPatientId(request.getPatientId())
                    .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND.withDescription(
                            "Billing account not found for patient ID: " + request.getPatientId())));

            account.setStatus("Inactive");
            billingAccountRepository.save(account);

            // Publish Kafka event
//            publishBillingEvent("BILLING_ACCOUNT_DEACTIVATED", account);

            // Build and send response
            DeactivateBillingResponse response = DeactivateBillingResponse.newBuilder()
                    .setStatus("Success")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to deactivate billing account for patient ID: {}. Error: {}", request.getPatientId(), e.getStatus());
            responseObserver.onError(e);
        } catch (Exception e) {
            logger.error("Unexpected error deactivating billing account for patient ID: {}", request.getPatientId(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException());
        }
    }


    private void validateBillingRequest(BillingRequest request) {
        if (request.getPatientId().isEmpty()) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
        }
        if (request.getName().isEmpty()) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Name cannot be empty"));
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid email format"));
        }
    }


    private String generateBillingAccountId() {
        return "BA-" + UUID.randomUUID().toString();
    }
}
