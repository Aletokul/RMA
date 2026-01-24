package com.example.sportfieldreservation.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportfieldreservation.R;
import com.example.sportfieldreservation.adapters.ReservationAdapter;
import com.example.sportfieldreservation.database.AppDatabase;
import com.example.sportfieldreservation.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReservationListActivity extends AppCompatActivity {

    private RecyclerView rvReservations;
    private ReservationAdapter adapter;
    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        rvReservations = findViewById(R.id.rvReservations);

        // ako iz nekog razloga layout na tabletu nema rvReservations → nemoj da rušiš app
        if (rvReservations == null) {
            Toast.makeText(
                    this,
                    "Ne može da se prikaže lista rezervacija na ovom layout-u.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        rvReservations.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReservationAdapter(new ArrayList<>(), this::showDeleteDialog);
        rvReservations.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        loadReservations();
    }

    private void loadReservations() {
        if (executorService == null) return;

        executorService.execute(() -> {
            List<Reservation> list = db.reservationDao().getAll();
            runOnUiThread(() -> adapter.setReservations(list));
        });
    }

    private void showDeleteDialog(Reservation reservation) {
        new AlertDialog.Builder(this)
                .setTitle("Brisanje rezervacije")
                .setMessage("Da li ste sigurni da želite da obrišete ovu rezervaciju?")
                .setPositiveButton("Da", (dialog, which) -> deleteReservation(reservation))
                .setNegativeButton("Ne", null)
                .show();
    }

    private void deleteReservation(Reservation reservation) {
        if (executorService == null) return;

        executorService.execute(() -> {
            db.reservationDao().delete(reservation);
            List<Reservation> updated = db.reservationDao().getAll();
            runOnUiThread(() -> adapter.setReservations(updated));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
