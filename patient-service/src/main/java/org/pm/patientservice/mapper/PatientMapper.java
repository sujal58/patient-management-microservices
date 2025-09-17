package org.pm.patientservice.mapper;

import org.pm.patientservice.dto.PatientRequestDto;
import org.pm.patientservice.dto.PatientResponseDto;
import org.pm.patientservice.model.Patient;

import java.time.LocalDate;

public class PatientMapper {

    public static PatientResponseDto toDto(Patient p){
        PatientResponseDto dto = new PatientResponseDto();
        dto.setId(p.getId().toString());
        dto.setName(p.getName());
        dto.setEmail(p.getEmail());
        dto.setAddress(p.getAddress());
        dto.setDateOfBirth(p.getDateOfBirth().toString());

        return dto;
    }

    public static Patient toPatient(PatientRequestDto patientRequestDto){
        Patient patient = new Patient();
        patient.setName(patientRequestDto.getName());
        patient.setEmail(patientRequestDto.getEmail());
        patient.setAddress(patientRequestDto.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientRequestDto.getRegisteredDate()));

        return patient;
    }
}
