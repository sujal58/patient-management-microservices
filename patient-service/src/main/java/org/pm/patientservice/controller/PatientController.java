package org.pm.patientservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.pm.patientservice.dto.PaginationResponse;
import org.pm.patientservice.dto.PatientRequestDto;
import org.pm.patientservice.dto.PatientResponseDto;
import org.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import org.pm.patientservice.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name="Patient", description = "API for managing Patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    @Operation(summary = "Get patients")
    @GetMapping
    public ResponseEntity<PaginationResponse<PatientResponseDto>> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nameFilter) {
        log.info("Received request to get patients: page={}, size={}, nameFilter={}", page, size, nameFilter);
        PaginationResponse<PatientResponseDto> response = patientService.getPatients(page, size, nameFilter);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new patients")
    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDto patientRequestDto) {
        log.info("Received request to create patient with email: {}", patientRequestDto.getEmail());
        PatientResponseDto savedPatient = patientService.createPatient(patientRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
    }

    @Operation(summary = "Update an existing patient", description = "Update patient details by ID")    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(@PathVariable UUID id,@Valid @RequestBody PatientRequestDto patientRequestDto) {
        log.info("Received request to update patient with ID: {}", id);
        PatientResponseDto patientResponseDto = patientService.updatePatient(id, patientRequestDto);
        return ResponseEntity.ok(patientResponseDto);
    }


    @Operation(summary = "Delete a patient", description = "Delete a patient by ID")    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable UUID id) {
        log.info("Received request to delete patient with ID: {}", id);
        patientService.deletePatient(id);
        Map<String, String> message = new HashMap<>();
        message.put("message", "Patient deleted successfully with id: " + id);
        return ResponseEntity.status(200).body(message);
    }


}
