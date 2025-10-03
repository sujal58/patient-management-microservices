package org.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.transaction.Transactional;
import org.pm.analyticsservice.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final AnalyticsService analyticsService;

    public KafkaConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    @Transactional
    public void consumeEvent(byte[] event){
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            log.info("Received Patient Event: [Type={}, PatientId={}, PatientName={}, PatientEmail={}, DateOfBirth={}]",
                    patientEvent.getEventType(),
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail(),
                    patientEvent.getDateOfBirth());

            // Process event based on type
            analyticsService.processPatientEvent(patientEvent);

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing event: {}", e.getMessage(), e);
        }
    }
}
