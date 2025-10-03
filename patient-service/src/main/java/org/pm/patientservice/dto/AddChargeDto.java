package org.pm.patientservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class AddChargeDto {
    @NotNull
    @Positive
    private double amount;
    @NotNull
    @NotEmpty
    private String description;
    @NotNull
    private LocalDate chargeDate;

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
}
