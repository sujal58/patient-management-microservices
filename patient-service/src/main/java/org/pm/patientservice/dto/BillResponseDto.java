package org.pm.patientservice.dto;

import java.time.LocalDate;
import java.util.List;

public class BillResponseDto {
    private String billId;
    private String patientId;
    private LocalDate billDate;
    private double totalAmount;
    private List<ChargeDto> charges;

    public BillResponseDto(String billId, String patientId, LocalDate billDate, double totalAmount, List<ChargeDto> charges) {
        this.billId = billId;
        this.patientId = patientId;
        this.billDate = billDate;
        this.totalAmount = totalAmount;
        this.charges = charges;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ChargeDto> getCharges() {
        return charges;
    }

    public void setCharges(List<ChargeDto> charges) {
        this.charges = charges;
    }

    // Getters and setters
}
