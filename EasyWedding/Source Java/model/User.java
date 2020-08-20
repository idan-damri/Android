package com.example.easywedding.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
// POJO that represent a message in a chat.
public class User {

    private String displayName;
    private String email;
    private Boolean invitesAllowed;
    private String chatId;
    private String dataId;
    private String owner;

    // <sender chatID, sender email>
    private Map<String, String> invitesReceived;

    //required for db
    public User(){

    }
    public User(String displayName, String email, Boolean invitesAllowed, String dataId){
        this.displayName = displayName;
        this.email = email;
        this.invitesAllowed = invitesAllowed;
        this.dataId = dataId;
    }
    public User(String displayName, String email, Boolean invitesAllowed, String dataId, String owner){
        this.displayName = displayName;
        this.email = email;
        this.invitesAllowed = invitesAllowed;
        this.dataId = dataId;
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwnerE(String owner) {
        this.owner = owner;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Boolean isInvitesAllowed() {
        return invitesAllowed;
    }

    public void setInvitesAllowed(Boolean invitesAllowed) {
        this.invitesAllowed = invitesAllowed;
    }

    public String getChatId() {
        return chatId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, String> getInvitesReceived() {
        return invitesReceived;
    }


    public void setInvitesReceived(Map<String, String> invitesReceived) {
        this.invitesReceived = invitesReceived;
    }

}
