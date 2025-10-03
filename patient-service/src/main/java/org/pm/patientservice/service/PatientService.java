package org.pm.patientservice.service;

import jakarta.transaction.Transactional;
import org.pm.patientservice.dto.PaginationResponse;
import org.pm.patientservice.dto.PatientRequestDto;
import org.pm.patientservice.dto.PatientResponseDto;
import org.pm.patientservice.enums.KafkaEvent;
import org.pm.patientservice.exception.BillingServiceException;
import org.pm.patientservice.exception.EmailAlreadyExistsException;
import org.pm.patientservice.exception.PatientNotFoundException;
import org.pm.patientservice.grpc.BillingServiceGrpcClient;
import org.pm.patientservice.kafka.KafkaProducer;
import org.pm.patientservice.mapper.PatientMapper;
import org.pm.patientservice.model.Patient;
import org.pm.patientservice.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(
            PatientRepository patientRepository,
            BillingServiceGrpcClient billingServiceGrpcClient,
            KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public PaginationResponse<PatientResponseDto> getPatients(int page, int size, String nameFilter){
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientsPage = (nameFilter == null || nameFilter.isEmpty())
                ? patientRepository.findAll(pageable)
                : patientRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
        List<PatientResponseDto> response = patientsPage.getContent().stream().map(PatientMapper::toDto).toList();
        return new PaginationResponse<>(response, page, size, patientsPage.getTotalElements());
    }

    @Transactional
    public PatientResponseDto createPatient(PatientRequestDto patientRequestDto){
        log.info("Creating patient with email: {}", patientRequestDto.getEmail());
        if(patientRepository.existsByEmail(patientRequestDto.getEmail())){
            throw new EmailAlreadyExistsException("A patient with this email is already exists "+ patientRequestDto.getEmail());
        }
        Patient newPatient = patientRepository.save(
                PatientMapper.toPatient(patientRequestDto)
        );

        try {
            // Create billing account via gRPC
            billingServiceGrpcClient.createBillingAccount(
                    newPatient.getId().toString(),
                    newPatient.getName(),
                    newPatient.getEmail()
            );
        } catch (Exception e) {
            log.error("Failed to create billing account for patient ID: {}", newPatient.getId(), e);
            // Compensating transaction: delete patient if billing fails
            patientRepository.delete(newPatient);
            throw new BillingServiceException("Failed to create billing account: " + e.getMessage());
        }

        try {
            // Publish Kafka event
            kafkaProducer.sendEvent(newPatient, KafkaEvent.PATIENT_CREATED);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for patient ID: {}", newPatient.getId(), e);
        }


        return PatientMapper.toDto(newPatient);
    }


    @Transactional
    public PatientResponseDto updatePatient(UUID id, PatientRequestDto patientRequestDto){
        log.info("Updating patient with ID: {}", id);
        Patient patient = patientRepository.findById(id).orElseThrow(
                 ()-> new PatientNotFoundException("Patient not found with ID: "+ id));

        if(patientRepository.existsByEmailAndIdNot(patientRequestDto.getEmail(), id)){
            throw new EmailAlreadyExistsException("A patient with this email is already exists "+ patientRequestDto.getEmail());
        }

        patient.setName(patientRequestDto.getName());
        patient.setEmail(patientRequestDto.getEmail());
        patient.setAddress(patientRequestDto.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);

        // Publish Kafka event for update
        try {
            kafkaProducer.sendEvent(updatedPatient, KafkaEvent.PATIENT_UPDATED);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for updated patient ID: {}", id, e);
        }
        return PatientMapper.toDto(updatedPatient);
    }

    @Transactional
    public void deletePatient(UUID id){
        log.info("Deleting patient with ID: {}", id);

        // Check if patient exists
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));

        // Delete patient
        patientRepository.deleteById(id);

        // Notify billing service to deactivate account
        try {
            billingServiceGrpcClient.deactivateBillingAccount(id.toString());
        } catch (Exception e) {
            log.error("Failed to deactivate billing account for patient ID: {}", id, e);
        }

        // Publish Kafka event for deletion
        try {
            kafkaProducer.sendEvent( patient, KafkaEvent.PATIENT_DELETED);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for deleted patient ID: {}", id, e);
        }
    }
}
