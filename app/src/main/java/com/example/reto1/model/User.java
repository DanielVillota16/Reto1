package com.example.reto1.model;

public class User {

    private String id;
    private Position location;

    public User() {
    }

    public User(String id, Position location) {
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Position getLocation() {
        return location;
    }

    public void setLocation(Position location) {
        this.location = location;
    }
}
