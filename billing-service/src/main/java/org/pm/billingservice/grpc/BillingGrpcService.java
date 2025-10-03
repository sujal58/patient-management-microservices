package org.pm.billingservice.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import net.devh.boot.grpc.server.service.GrpcService;
import org.pm.billingservice.model.BillingAccount;
import org.pm.billingservice.repository.BillRepository;
import org.pm.billingservice.repository.BillingAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import org.pm.billingservice.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BillingGrpcService.class);

    private final BillingAccountRepository billingAccountRepository;
    private final BillRepository billRepository;

    public BillingGrpcService(BillingAccountRepository billingAccountRepository, BillRepository billRepository) {
        this.billingAccountRepository = billingAccountRepository;
        this.billRepository = billRepository;
    }

    @Override
    @Transactional
    public void createBillingAccount(BillingRequest request, StreamObserver<BillingResponse> responseObserver) {
        logger.info("Received createBillingAccount request for patient ID: {}", request.getPatientId());

        try {
            validateBillingRequest(request);

            if (billingAccountRepository.existsByPatientId(request.getPatientId())) {
                throw new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription(
                        "Billing account already exists for patient ID: " + request.getPatientId()));
            }

            BillingAccount account = new BillingAccount();
            account.setPatientId(request.getPatientId());
            account.setName(request.getName());
            account.setEmail(request.getEmail());
            account.setStatus("Active");
            account.setBillingAccountId(generateBillingAccountId());
            billingAccountRepository.save(account);

            BillingResponse response = BillingResponse.newBuilder()
                    .setBillingAccountId(account.getBillingAccountId())
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
            if (request.getPatientId().isEmpty()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
            }

            BillingAccount account = billingAccountRepository.findByPatientId(request.getPatientId())
                    .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND.withDescription(
                            "Billing account not found for patient ID: " + request.getPatientId())));

            account.setStatus("Inactive");
            billingAccountRepository.save(account);

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

    @Override
    @Transactional
    public void addCharge(AddChargeRequest request, StreamObserver<BillResponse> responseObserver) {
        logger.info("Received addCharge request for patient ID: {}", request.getPatientId());

        try {
            validateAddChargeRequest(request);

            BillingAccount account = billingAccountRepository.findByPatientId(request.getPatientId())
                    .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND.withDescription(
                            "Billing account not found for patient ID: " + request.getPatientId())));

            LocalDate billDate = LocalDate.parse(request.getChargeDate());
            Bill bill = billRepository.findByPatientIdAndBillDate(request.getPatientId(), billDate)
                    .orElseGet(() -> {
                        Bill newBill = new Bill();
                        newBill.setPatientId(request.getPatientId());
                        newBill.setBillDate(billDate);
                        newBill.setBillingAccount(account);
                        return newBill;
                    });

            org.pm.billingservice.model.Charge charge = new org.pm.billingservice.model.Charge();
//            charge.setChargeId("CH-" + UUID.randomUUID());
            charge.setAmount(request.getAmount());
            charge.setDescription(request.getDescription());
            charge.setChargeDate(billDate);
            bill.addCharge(charge);

            billRepository.save(bill);

            BillResponse response = buildBillResponse(bill);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to add charge for patient ID: {}. Error: {}", request.getPatientId(), e.getStatus());
            responseObserver.onError(e);
        } catch (Exception e) {
            logger.error("Unexpected error adding charge for patient ID: {}", request.getPatientId(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getBillsByPatient(GetBillsByPatientRequest request, StreamObserver<BillsResponse> responseObserver) {
        logger.info("Received getBillsByPatient request for patient ID: {}", request.getPatientId());

        try {
            if (request.getPatientId().isEmpty()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
            }

            Page<Bill> bills = billRepository.findByPatientId(request.getPatientId(),
                    PageRequest.of(request.getPage(), request.getSize()));
            BillsResponse response = BillsResponse.newBuilder()
                    .addAllBills(bills.getContent().stream().map(this::buildBillResponse).toList())
                    .setPage(request.getPage())
                    .setSize(request.getSize())
                    .setTotalBills(bills.getTotalElements())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get bills for patient ID: {}. Error: {}", request.getPatientId(), e.getStatus());
            responseObserver.onError(e);
        } catch (Exception e) {
            logger.error("Unexpected error getting bills for patient ID: {}", request.getPatientId(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getBillByPatientAndDate(GetBillByPatientAndDateRequest request, StreamObserver<BillResponse> responseObserver) {
        logger.info("Received getBillByPatientAndDate request for patient ID: {}, date: {}", request.getPatientId(), request.getBillDate());

        try {
            if (request.getPatientId().isEmpty()) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
            }
            LocalDate billDate = LocalDate.parse(request.getBillDate());

            Bill bill = billRepository.findByPatientIdAndBillDate(request.getPatientId(), billDate)
                    .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND.withDescription(
                            "Bill not found for patient ID: " + request.getPatientId() + " and date: " + request.getBillDate())));

            BillResponse response = buildBillResponse(bill);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get bill for patient ID: {}, date: {}. Error: {}", request.getPatientId(), request.getBillDate(), e.getStatus());
            responseObserver.onError(e);
        } catch (Exception e) {
            logger.error("Unexpected error getting bill for patient ID: {}, date: {}", request.getPatientId(), request.getBillDate(), e);
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

    private void validateAddChargeRequest(AddChargeRequest request) {
        if (request.getPatientId().isEmpty()) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
        }
        if (request.getAmount() <= 0) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Amount must be positive"));
        }
        if (request.getDescription().isEmpty()) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Description cannot be empty"));
        }
        try {
            LocalDate.parse(request.getChargeDate());
        } catch (Exception e) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid charge date format"));
        }
    }

    private BillResponse buildBillResponse(Bill bill) {
        return BillResponse.newBuilder()
                .setBillId(bill.getBillId())
                .setPatientId(bill.getPatientId())
                .setBillDate(bill.getBillDate().toString())
                .setTotalAmount(bill.getTotalAmount())
                .addAllCharges(bill.getCharges().stream().map(charge -> Charge.newBuilder()
                        .setChargeId(charge.getChargeId())
                        .setAmount(charge.getAmount())
                        .setDescription(charge.getDescription())
                        .setChargeDate(charge.getChargeDate().toString())
                        .build()).toList())
                .build();
    }


    private String generateBillingAccountId() {
        return "BA-" + UUID.randomUUID();
    }
}



//@GrpcService
//public class BillingGrpcService extends BillingServiceImplBase {
//
//    private static final Logger logger = LoggerFactory.getLogger(BillingGrpcService.class);
//
//    private final BillingAccountRepository billingAccountRepository;
//
//    public BillingGrpcService(BillingAccountRepository billingAccountRepository) {
//        this.billingAccountRepository = billingAccountRepository;
//    }
//
//    @Override
//    @Transactional
//    public void createBillingAccount(BillingRequest request, StreamObserver<BillingResponse> responseObserver) {
//        logger.info("Received createBillingAccount request for patient ID: {}", request.getPatientId());
//
//        try {
//            // Validate request
////            validateBillingRequest(request);
//
//            // Check for existing account
//            if (billingAccountRepository.existsByPatientId(request.getPatientId())) {
//                throw new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription(
//                        "Billing account already exists for patient ID: " + request.getPatientId()));
//            }
//
//            // Create and save billing account
//            BillingAccount account = new BillingAccount();
//            account.setPatientId(request.getPatientId());
//            account.setName(request.getName());
//            account.setEmail(request.getEmail());
//            account.setStatus("Active");
//            account.setBillingAccountId(generateBillingAccountId()); // Generate unique ID
//            account = billingAccountRepository.save(account);
//
//            // Publish Kafka event
////            publishBillingEvent("BILLING_ACCOUNT_CREATED", account);
//
//            // Build and send response
//            BillingResponse response = BillingResponse.newBuilder()
//                    .setAccountId(account.getBillingAccountId())
//                    .setPatientId(account.getPatientId())
//                    .setStatus(account.getStatus())
//                    .build();
//
//            responseObserver.onNext(response);
//            responseObserver.onCompleted();
//        } catch (StatusRuntimeException e) {
//            logger.error("Failed to create billing account for patient ID: {}. Error: {}", request.getPatientId(), e.getStatus());
//            responseObserver.onError(e);
//        } catch (Exception e) {
//            logger.error("Unexpected error creating billing account for patient ID: {}", request.getPatientId(), e);
//            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException());
//        }
//    }
//
//
//    @Override
//    @Transactional
//    public void deactivateBillingAccount(DeactivateBillingRequest request, StreamObserver<DeactivateBillingResponse> responseObserver) {
//        logger.info("Received deactivateBillingAccount request for patient ID: {}", request.getPatientId());
//
//        try {
//            // Validate request
//            if (request.getPatientId().isEmpty()) {
//                throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
//            }
//
//            // Find and deactivate account
//            BillingAccount account = billingAccountRepository.findByPatientId(request.getPatientId())
//                    .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND.withDescription(
//                            "Billing account not found for patient ID: " + request.getPatientId())));
//
//            account.setStatus("Inactive");
//            billingAccountRepository.save(account);
//
//            // Publish Kafka event
////            publishBillingEvent("BILLING_ACCOUNT_DEACTIVATED", account);
//
//            // Build and send response
//            DeactivateBillingResponse response = DeactivateBillingResponse.newBuilder()
//                    .setStatus("Success")
//                    .build();
//
//            responseObserver.onNext(response);
//            responseObserver.onCompleted();
//        } catch (StatusRuntimeException e) {
//            logger.error("Failed to deactivate billing account for patient ID: {}. Error: {}", request.getPatientId(), e.getStatus());
//            responseObserver.onError(e);
//        } catch (Exception e) {
//            logger.error("Unexpected error deactivating billing account for patient ID: {}", request.getPatientId(), e);
//            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException());
//        }
//    }
//
//
//    private void validateBillingRequest(BillingRequest request) {
//        if (request.getPatientId().isEmpty()) {
//            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Patient ID cannot be empty"));
//        }
//        if (request.getName().isEmpty()) {
//            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Name cannot be empty"));
//        }
//        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
//            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid email format"));
//        }
//    }
//
//
//    private String generateBillingAccountId() {
//        return "BA-" + UUID.randomUUID().toString();
//    }
//}
