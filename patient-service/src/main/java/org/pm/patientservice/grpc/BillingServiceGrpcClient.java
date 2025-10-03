package org.pm.patientservice.grpc;


import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import billing.DeactivateBillingRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.pm.patientservice.exception.BillingServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
}
