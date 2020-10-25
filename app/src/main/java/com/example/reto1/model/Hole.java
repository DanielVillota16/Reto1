package com.example.reto1.model;

public class Hole {

    private String id;
    private String userID;
    private Position location;
    private boolean isConfirmed;

    public Hole() {
    }

    public Hole(String id, String userID, Position location, boolean isConfirmed) {
        this.id = id;
        this.userID = userID;
        this.location = location;
        this.isConfirmed = isConfirmed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Position getLocation() {
        return location;
    }

    public void setLocation(Position location) {
        this.location = location;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }
}
