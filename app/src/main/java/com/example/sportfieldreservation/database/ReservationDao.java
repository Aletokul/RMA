package com.example.sportfieldreservation.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.sportfieldreservation.model.Reservation;

import java.util.List;

@Dao
public interface ReservationDao {

    @Insert
    void insert(Reservation reservation);

    @Query("SELECT * FROM reservations ORDER BY id DESC")
    List<Reservation> getAll();

    @Delete
    void delete(Reservation reservation);
}
