package org.pm.analyticsservice.dto;

import java.time.LocalDate;

public class AnalyticsResponseDto {
    private long totalPatients;
    private long newPatients;
    private double averageAge;

    public AnalyticsResponseDto(long totalPatients, long newPatients, double averageAge) {
        this.totalPatients = totalPatients;
        this.newPatients = newPatients;
        this.averageAge = averageAge;
    }

    // Getters and setters
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

    public double getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(double averageAge) {
        this.averageAge = averageAge;
    }
}
