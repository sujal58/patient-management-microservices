package org.pm.billingservice.model;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Charge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String chargeId;
    private double amount;
    private String description;
    private LocalDate chargeDate;


    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;

    // Getters and setters
    public String getChargeId() { return chargeId; }
    public void setChargeId(String chargeId) { this.chargeId = chargeId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getChargeDate() { return chargeDate; }
    public void setChargeDate(LocalDate chargeDate) { this.chargeDate = chargeDate; }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
