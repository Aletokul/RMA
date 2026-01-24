package com.example.sportfieldreservation.model;

public class Field {

    private String name;
    private String type;      // npr. fudbal, košarka, tenis
    private String location;  // adresa ili naziv lokacije
    private int pricePerHour; // cena po satu u dinarima

    // Prazan konstruktor – potreban za Gson
    public Field() {
    }

    public Field(String name, String type, String location, int pricePerHour) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.pricePerHour = pricePerHour;
    }

    // GETTERI
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public int getPricePerHour() {
        return pricePerHour;
    }

    // SETTERI – za Gson (može bez njih, ali je ovako uredno)
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPricePerHour(int pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
}
