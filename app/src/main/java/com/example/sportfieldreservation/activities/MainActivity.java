package com.example.sportfieldreservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportfieldreservation.R;

public class MainActivity extends AppCompatActivity {

    private Button btnFields;
    private Button btnReservations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFields = findViewById(R.id.btnFields);
        btnReservations = findViewById(R.id.btnReservations);

        btnFields.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FieldListActivity.class);
            startActivity(intent);
        });

        btnReservations.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReservationListActivity.class);
            startActivity(intent);
        });


    }
}
