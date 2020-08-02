package com.example.easywedding.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Supplier implements Parcelable {
    private String supplierName;
    private String supplierPhoneNumber;
    private String supplierEmail;

    public Supplier(){

    }

    public Supplier(String supplierName, String supplierPhoneNumber, String supplierEmail) {
        this.supplierName = supplierName;
        this.supplierPhoneNumber = supplierPhoneNumber;
        this.supplierEmail = supplierEmail;
    }

    protected Supplier(Parcel in) {
        supplierName = in.readString();
        supplierPhoneNumber = in.readString();
        supplierEmail = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(supplierName);
        dest.writeString(supplierPhoneNumber);
        dest.writeString(supplierEmail);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Supplier> CREATOR = new Creator<Supplier>() {
        @Override
        public Supplier createFromParcel(Parcel in) {
            return new Supplier(in);
        }

        @Override
        public Supplier[] newArray(int size) {
            return new Supplier[size];
        }
    };

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierPhoneNumber() {
        return supplierPhoneNumber;
    }

    public void setSupplierPhoneNumber(String supplierPhoneNumber) {
        this.supplierPhoneNumber = supplierPhoneNumber;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

}
