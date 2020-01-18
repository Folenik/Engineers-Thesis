package com.kamilmosz.projektinzynierski.Models;

public class ScannedElements {

    public String id, status, name;

    public ScannedElements() {

    }

    public ScannedElements(String id, String status) {
        this.id = id;
        //this.name = name;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
