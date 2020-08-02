package com.example.easywedding.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Feature implements Parcelable {

    private String name;
    private String supplierId;
    private String advancePayment;
    private String paymentBalance;
    private String freeText;
    private String quantity;

    public Feature(){

    }
    public Feature(String name, String supplierId, String freeText
                   ,String advancePayment, String paymentBalance, String quantity) {
        this.name = name;
        this.supplierId = supplierId;
        this.freeText = freeText;
        this.advancePayment = advancePayment;
        this.paymentBalance = paymentBalance;
        this.quantity = quantity;
    }
    protected Feature(Parcel in) {
        name = in.readString();
        supplierId = in.readString();
        advancePayment = in.readString();
        paymentBalance = in.readString();
        freeText = in.readString();
        quantity = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(supplierId);
        dest.writeString(advancePayment);
        dest.writeString(paymentBalance);
        dest.writeString(freeText);
        dest.writeString(quantity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Feature> CREATOR = new Creator<Feature>() {
        @Override
        public Feature createFromParcel(Parcel in) {
            return new Feature(in);
        }

        @Override
        public Feature[] newArray(int size) {
            return new Feature[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getFreeText() {
        return freeText;
    }


    public String getAdvancePayment() {
        return advancePayment;
    }

    public String getPaymentBalance() {
        return paymentBalance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }


    public void setAdvancePayment(String advancePayment) {
        this.advancePayment = advancePayment;
    }

    public void setPaymentBalance(String paymentBalance) {
        this.paymentBalance = paymentBalance;
    }
    
}
