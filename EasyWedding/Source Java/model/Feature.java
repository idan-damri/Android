package com.example.easywedding.model;

public class Feature {

    private String name;
    private String freeText;
    private String supplierName;
    private String supplierPhoneNumber;
    private int advancePayment;
    private int paymentBalance;

    public Feature(String name, String freeText, String supplierName
                   , String supplierPhoneNumber, int advancePayment, int paymentBalance) {
        this.name = name;
        this.freeText = freeText;
        this.supplierName = supplierName;
        this.supplierPhoneNumber = supplierPhoneNumber;
        this.advancePayment = advancePayment;
        this.paymentBalance = paymentBalance;
    }

    public String getName() {
        return name;
    }

    public String getFreeText() {
        return freeText;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierPhoneNumber() {
        return supplierPhoneNumber;
    }

    public int getAdvancePayment() {
        return advancePayment;
    }

    public int getPaymentBalance() {
        return paymentBalance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public void setSupplierPhoneNumber(String supplierPhoneNumber) {
        this.supplierPhoneNumber = supplierPhoneNumber;
    }

    public void setAdvancePayment(int advancePayment) {
        this.advancePayment = advancePayment;
    }

    public void setPaymentBalance(int paymentBalance) {
        this.paymentBalance = paymentBalance;
    }
    
}
