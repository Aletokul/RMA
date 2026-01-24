package com.example.sportfieldreservation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportfieldreservation.R;

public class FieldDetailActivity extends AppCompatActivity {

    private TextView tvFieldName;
    private TextView tvFieldType;
    private TextView tvFieldLocation;
    private TextView tvFieldPrice;
    private Button btnReserveField;

    private String fieldName;
    private String fieldLocation; // ➜ čuvamo čistu lokaciju za mapu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_detail);

        tvFieldName = findViewById(R.id.tvFieldName);
        tvFieldType = findViewById(R.id.tvFieldType);
        tvFieldLocation = findViewById(R.id.tvFieldLocation);
        tvFieldPrice = findViewById(R.id.tvFieldPrice);
        btnReserveField = findViewById(R.id.btnReserveField);

        Intent intent = getIntent();
        fieldName = intent.getStringExtra("field_name");
        String type = intent.getStringExtra("field_type");
        fieldLocation = intent.getStringExtra("field_location"); // ➜ sačuvaj u polje
        int price = intent.getIntExtra("field_price", 0);

        if (fieldName == null || fieldName.trim().isEmpty()) {
            fieldName = "Nepoznat teren";
        }

        tvFieldName.setText(fieldName);

        if (type == null || type.trim().isEmpty()) {
            tvFieldType.setText("Tip: -");
        } else {
            tvFieldType.setText("Tip: " + type);
        }

        if (fieldLocation == null || fieldLocation.trim().isEmpty()) {
            tvFieldLocation.setText("Lokacija: -");
        } else {
            tvFieldLocation.setText("Lokacija: " + fieldLocation);
        }

        if (price > 0) {
            tvFieldPrice.setText("Cena: " + price + " RSD/h");
        } else {
            tvFieldPrice.setText("Cena: -");
        }

        // ➜ klik na lokaciju otvara mape
        tvFieldLocation.setOnClickListener(v -> openInMaps());

        // ➜ već postojeći kod za rezervaciju
        btnReserveField.setOnClickListener(v -> {
            Intent reserveIntent = new Intent(FieldDetailActivity.this, AddReservationActivity.class);
            reserveIntent.putExtra("field_name", fieldName);
            reserveIntent.putExtra("field_price_per_hour", price); // <-- ovo obavezno
            startActivity(reserveIntent);
            overridePendingTransition(R.anim.slide_in_down, 0);
        });



    }

    private void openInMaps() {
        if (fieldLocation == null || fieldLocation.trim().isEmpty()) {
            return; // nema lokacije, nema ni otvaranja
        }

        // šta će da se traži u mapama
        String query;
        if (fieldName != null && !fieldName.trim().isEmpty() && !"Nepoznat teren".equals(fieldName)) {
            query = fieldName + " " + fieldLocation; // npr. "Balon Detelinara Novi Sad"
        } else {
            query = fieldLocation; // npr. "Novi Sad"
        }

        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }



}
