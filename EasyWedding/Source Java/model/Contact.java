package com.example.easywedding.model;

import java.util.Comparator;
import java.util.Objects;

public class Contact implements Comparable<Contact> {

    private String name;
    private String phoneNumber;
    private String photo;

    //true if the contact is invited to the wedding
    private boolean invited;
    private String uid;



    public Contact(String name, String phoneNumber, String photo, boolean invited, String uid) {
        this.name = name.trim();
        this.phoneNumber = phoneNumber;
        this.photo = photo;
        this.invited = invited;
        this.uid = uid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return name.equals(contact.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Contact other) {
        return this.getName().compareTo(other.getName());
    }

    public Guest getGuestInstanceFromContact(){
        return new Guest(name, phoneNumber, 0,"", false, false, "0", false);
    }
}
