package com.example.easywedding.model;

public class Guest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private int priority;
    private boolean arrive;

    public Guest(String firstName, String lastName, String phoneNumber, int priority, boolean arrive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.priority = priority;
        this.arrive = arrive;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isArrive() {
        return arrive;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setArrive(boolean arrive) {
        this.arrive = arrive;
    }
}
