package org.pm.analyticsservice.service;

import com.google.protobuf.InvalidProtocolBufferException;
import org.pm.analyticsservice.dto.AnalyticsResponseDto;
import org.pm.analyticsservice.exception.AnalyticsNotFoundException;
import org.pm.analyticsservice.model.AnalyticsData;
import org.pm.analyticsservice.repository.AnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patient.events.PatientEvent;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }


    /**
     * process the patient evennt and update the analyrics data
     */
    public void processPatientEvent(PatientEvent patientEvent) {
        String eventType = patientEvent.getEventType();
        String patientId = patientEvent.getPatientId();
        String dateOfBirthStr = patientEvent.getDateOfBirth();

        // Find or create daily analytics entry (use current date for simplicity; ideally use event timestamp)
        LocalDate eventDate = LocalDate.now();
        AnalyticsData analyticsData = analyticsRepository.findByDate(eventDate)
                .orElseGet(() -> new AnalyticsData(eventDate));

        int age = -1; // Invalid age marker
        if (!dateOfBirthStr.isEmpty()) {
            try {
                LocalDate dob = LocalDate.parse(dateOfBirthStr);
                age = Period.between(dob, LocalDate.now()).getYears();
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date of birth format for patient ID: {}. Skipping age calculation.", patientId);
            }
        }

        switch (eventType) {
            case "PATIENT_CREATED":
                analyticsData.incrementNewPatients();
                analyticsData.incrementTotalPatients();
                if (age >= 0) {
                    analyticsData.updateAverageAge(age);
                }
                break;
            case "PATIENT_UPDATED":
                // For updates, we could fetch the patient or assume minimal impact
                // For now, if DOB provided and changed, update age (simplified)
                if (age >= 0) {
                    logger.info("Updating age analytics for patient ID: {}", patientId);
                    analyticsData.updateAverageAge(age); // Assumes we adjust aggregate; in reality, may need delta
                }
                break;
            case "PATIENT_DELETED":
                analyticsData.decrementTotalPatients();
                break;
            default:
                logger.warn("Unknown event type: {} for patient ID: {}", eventType, patientId);
                return;
        }

        analyticsRepository.save(analyticsData);
        logger.info("Updated analytics for date: {}", eventDate);
    }

    /**
     * Retrieves aggregated analytics stats for a date range.
     */
    public AnalyticsResponseDto getAnalyticsStats(LocalDate fromDate, LocalDate toDate) {
        System.out.println("From date: "+ fromDate);
        System.out.println("To date: "+ toDate);
        List<AnalyticsData> dataList;

        if(fromDate == null || toDate == null) {
            dataList = analyticsRepository.findAll();
        }else{
            dataList = analyticsRepository.findByDateBetween(fromDate, toDate);
        }
        if (dataList.isEmpty()) {
            throw new AnalyticsNotFoundException("No analytics data found for the specified range: " + fromDate + " to " + toDate);
        }

        // Aggregate data
        long totalPatients = dataList.stream().mapToLong(AnalyticsData::getTotalPatients).sum();
        long newPatients = dataList.stream().mapToLong(AnalyticsData::getNewPatients).sum();
        double sumAges = dataList.stream().mapToDouble(AnalyticsData::getSumAges).sum();
        long totalPatientCountForAge = dataList.stream().mapToLong(AnalyticsData::getPatientCountForAge).sum();
        double averageAge = totalPatientCountForAge > 0 ? sumAges / totalPatientCountForAge : 0.0;

        return new AnalyticsResponseDto(totalPatients, newPatients, averageAge);
    }
}
