package org.pm.billingservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String billId;
    private String patientId;
    private LocalDate billDate;
    private double totalAmount;

    @ManyToOne
    @JoinColumn(name = "billing_account_id")
    private BillingAccount billingAccount;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "bill")
    private List<Charge> charges = new ArrayList<>();

    public void addCharge(Charge charge) {
        charges.add(charge);
        charge.setBill(this);
        totalAmount += charge.getAmount();
    }

    // Getters and setters
    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public BillingAccount getBillingAccount() { return billingAccount; }
    public void setBillingAccount(BillingAccount billingAccount) { this.billingAccount = billingAccount; }
    public List<Charge> getCharges() { return charges; }
    public void setCharges(List<Charge> charges) { this.charges = charges; }
}
