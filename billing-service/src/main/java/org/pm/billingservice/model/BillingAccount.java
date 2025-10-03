package org.pm.billingservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BillingAccount {
    @Id
    private String billingAccountId;
    private String patientId;
    private String name;
    private String email;
    private String status;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "billingAccount")
    private List<Bill> bills = new ArrayList<>();

    // Getters and setters
    public String getBillingAccountId() { return billingAccountId; }
    public void setBillingAccountId(String billingAccountId) { this.billingAccountId = billingAccountId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Bill> getBills() { return bills; }
    public void setBills(List<Bill> bills) { this.bills = bills; }
}

//@Entity
//public class BillingAccount {
//    @Id
//    private String billingAccountId;
//    private String patientId;
//    private String name;
//    private String email;
//    private String status;
//
//    public String getBillingAccountId() {
//        return billingAccountId;
//    }
//
//    public void setBillingAccountId(String billingAccountId) {
//        this.billingAccountId = billingAccountId;
//    }
//
//    public String getPatientId() {
//        return patientId;
//    }
//
//    public void setPatientId(String patientId) {
//        this.patientId = patientId;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//}
