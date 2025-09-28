package org.pm.analyticsservice.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class AnalyticsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private long totalPatients = 0;
    private long newPatients = 0;
    private double sumAges = 0.0;
    private long patientCountForAge = 0;

    public AnalyticsData() {}

    public AnalyticsData(LocalDate date) {
        this.date = date;
    }

    public void incrementTotalPatients() {
        this.totalPatients++;
    }

    public void decrementTotalPatients() {
        if (this.totalPatients > 0) {
            this.totalPatients--;
        }
    }

    public void incrementNewPatients() {
        this.newPatients++;
    }

    public void updateAverageAge(int age) {
        this.sumAges += age;
        this.patientCountForAge++;
    }

    public double getAverageAge() {
        return this.patientCountForAge > 0 ? this.sumAges / this.patientCountForAge : 0.0;
    }

    public double getSumAges() {
        return sumAges;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getNewPatients() {
        return newPatients;
    }

    public void setNewPatients(long newPatients) {
        this.newPatients = newPatients;
    }

    public void setSumAges(double sumAges) {
        this.sumAges = sumAges;
    }

    public long getPatientCountForAge() {
        return patientCountForAge;
    }

    public void setPatientCountForAge(long patientCountForAge) {
        this.patientCountForAge = patientCountForAge;
    }

}
