package com.example.sportfieldreservation.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reservations")
public class Reservation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fieldName;
    private String date;        // npr. 21.11.2025
    private String startTime;   // npr. 18:00
    private String endTime;     // npr. 19:30
    private String note;
    private double totalPrice;  // ukupna cena termina

    public Reservation(
            String fieldName,
            String date,
            String startTime,
            String endTime,
            String note,
            double totalPrice
    ) {
        this.fieldName = fieldName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
        this.totalPrice = totalPrice;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getFieldName() { return fieldName; }

    public String getDate() { return date; }

    public String getStartTime() { return startTime; }

    public String getEndTime() { return endTime; }

    public String getNote() { return note; }

    public double getTotalPrice() { return totalPrice; }

    // zbog starog koda koji koristi getTime()
    public String getTime() { return startTime; }
}
