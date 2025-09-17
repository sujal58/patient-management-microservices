package org.pm.patientservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.apache.commons.lang3.builder.ToStringSummary;
import org.pm.patientservice.dto.PatientRequestDto;
import org.pm.patientservice.dto.PatientResponseDto;
import org.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import org.pm.patientservice.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name="Patient", description = "API for managing Patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    @Operation(summary = "Get patients")
    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> getPatients() {
        List<PatientResponseDto> patients = patientService.getPatients();
        return ResponseEntity.ok(patients);
    }

    @Operation(summary = "Create new patients")
    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDto patientRequestDto) {
        PatientResponseDto savedPatient = patientService.createPatient(patientRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
    }

    @Operation(summary = "Update an existing patents")
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(@PathVariable UUID id,@Valid @RequestBody PatientRequestDto patientRequestDto) {

        PatientResponseDto patientResponseDto = patientService.updatePatient(id, patientRequestDto);
        return ResponseEntity.ok(patientResponseDto);
    }


    @Operation(summary = "Delete  a patients")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }


}
