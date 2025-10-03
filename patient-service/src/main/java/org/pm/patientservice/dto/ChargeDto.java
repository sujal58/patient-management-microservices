package org.pm.patientservice.dto;

import java.time.LocalDate;

public class ChargeDto {
    private String chargeId;
    private double amount;
    private String description;
    private LocalDate chargeDate;

    public ChargeDto(String chargeId, double amount, String description, LocalDate chargeDate) {
        this.chargeId = chargeId;
        this.amount = amount;
        this.description = description;
        this.chargeDate = chargeDate;
    }

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getChargeDate() {
        return chargeDate;
    }

    public void setChargeDate(LocalDate chargeDate) {
        this.chargeDate = chargeDate;
    }

    // Getters and setters
}
