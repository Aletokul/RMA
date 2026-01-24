package com.example.sportfieldreservation.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sportfieldreservation.R;
import com.example.sportfieldreservation.database.AppDatabase;
import com.example.sportfieldreservation.fragments.DatePickerFragment;
import com.example.sportfieldreservation.fragments.TimePickerFragment;
import com.example.sportfieldreservation.model.Reservation;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddReservationActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "reservation_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_POST_NOTIFICATIONS = 100;

    private Button btnSaveReservation;
    private EditText etDate, etTime, etEndTime, etNote;

    private ExecutorService executorService;

    private boolean dateSelected = false;
    private boolean timeSelected = false;
    private boolean endTimeSelected = false;

    private int selectedYear, selectedMonth, selectedDay;
    private int selectedStartHour, selectedStartMinute;
    private int selectedEndHour, selectedEndMinute;

    private String fieldName;
    private double pricePerHour;
    private double lastCalculatedPrice;

    private String lastReservationDate;
    private String lastReservationStartTime;
    private String lastReservationEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reservation);

        btnSaveReservation = findViewById(R.id.btnSaveReservation);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etEndTime = findViewById(R.id.etEndTime);
        etNote = findViewById(R.id.etNote);

        executorService = Executors.newSingleThreadExecutor();

        initExtras();
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> {
            if (!dateSelected) {
                Toast.makeText(this, "Prvo izaberi datum", Toast.LENGTH_SHORT).show();
            } else {
                showTimePickerForStart();
            }
        });
        etEndTime.setOnClickListener(v -> {
            if (!dateSelected) {
                Toast.makeText(this, "Prvo izaberi datum", Toast.LENGTH_SHORT).show();
            } else if (!timeSelected) {
                Toast.makeText(this, "Prvo izaberi vreme početka", Toast.LENGTH_SHORT).show();
            } else {
                showTimePickerForEnd();
            }
        });

        btnSaveReservation.setOnClickListener(v -> saveReservation());
    }

    private void initExtras() {
        fieldName = getIntent().getStringExtra("field_name");
        if (fieldName == null || fieldName.trim().isEmpty()) {
            fieldName = "Nepoznat teren";
        }

        double price = getIntent().getDoubleExtra("field_price_per_hour", -1.0);
        if (price <= 0) price = getIntent().getDoubleExtra("field_price", -1.0);

        if (price <= 0) {
            int priceInt = getIntent().getIntExtra("field_price_per_hour", -1);
            if (priceInt <= 0) priceInt = getIntent().getIntExtra("field_price", -1);
            if (priceInt > 0) price = priceInt;
        }

        pricePerHour = Math.max(price, 0.0);

        if (pricePerHour == 0.0) {
            Toast.makeText(this,
                    "Upozorenje: cena po satu je 0 (nije prosleđena iz prethodnog ekrana).",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_POST_NOTIFICATIONS
            );
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Rezervacije",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Obaveštenja o rezervacijama");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void showDatePicker() {
        DatePickerFragment fragment = new DatePickerFragment((year, month, day) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;

            lastReservationDate = String.format("%02d.%02d.%04d", day, month + 1, year);
            etDate.setText(lastReservationDate);

            dateSelected = true;
            timeSelected = false;
            endTimeSelected = false;
            etTime.setText("");
            etEndTime.setText("");
        });

        fragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void showTimePickerForStart() {
        TimePickerFragment fragment = new TimePickerFragment((hour, minute) -> {
            Calendar now = Calendar.getInstance();
            Calendar chosen = Calendar.getInstance();
            chosen.set(selectedYear, selectedMonth, selectedDay, hour, minute, 0);

            if (chosen.before(now)) {
                Toast.makeText(this, "Ne možeš rezervisati termin u prošlosti", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedStartHour = hour;
            selectedStartMinute = minute;
            lastReservationStartTime = String.format("%02d:%02d", hour, minute);
            etTime.setText(lastReservationStartTime);

            timeSelected = true;
            endTimeSelected = false;
            etEndTime.setText("");
        });

        fragment.show(getSupportFragmentManager(), "timePickerStart");
    }

    private void showTimePickerForEnd() {
        TimePickerFragment fragment = new TimePickerFragment((hour, minute) -> {
            Calendar chosenStart = Calendar.getInstance();
            chosenStart.set(selectedYear, selectedMonth, selectedDay,
                    selectedStartHour, selectedStartMinute, 0);

            Calendar chosenEnd = Calendar.getInstance();
            chosenEnd.set(selectedYear, selectedMonth, selectedDay, hour, minute, 0);

            if (!chosenEnd.after(chosenStart)) {
                Toast.makeText(this, "Kraj termina mora biti posle početka", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedEndHour = hour;
            selectedEndMinute = minute;
            lastReservationEndTime = String.format("%02d:%02d", hour, minute);
            etEndTime.setText(lastReservationEndTime);

            endTimeSelected = true;
        });

        fragment.show(getSupportFragmentManager(), "timePickerEnd");
    }

    private double calculateTotalPrice() {
        Calendar startCal = Calendar.getInstance();
        startCal.set(selectedYear, selectedMonth, selectedDay,
                selectedStartHour, selectedStartMinute, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(selectedYear, selectedMonth, selectedDay,
                selectedEndHour, selectedEndMinute, 0);

        long diffMillis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        if (diffMillis <= 0) return 0.0;

        double hours = (diffMillis / (1000.0 * 60 * 60));
        double billedHours = Math.ceil(hours);

        return billedHours * pricePerHour;
    }

    private void saveReservation() {
        if (!dateSelected || !timeSelected || !endTimeSelected) {
            Toast.makeText(this, "Izaberi datum, početak i kraj termina", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = etDate.getText().toString().trim();
        String startTime = etTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        lastCalculatedPrice = calculateTotalPrice();

        Reservation reservation = new Reservation(
                fieldName,
                date,
                startTime,
                endTime,
                note,
                lastCalculatedPrice
        );

        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.reservationDao().insert(reservation);

            runOnUiThread(() -> {
                Toast.makeText(
                        AddReservationActivity.this,
                        String.format("Rezervacija sačuvana\nCena: %.0f RSD", lastCalculatedPrice),
                        Toast.LENGTH_LONG
                ).show();

                showReservationNotification();
                finish();
            });
        });
    }

    private void showReservationNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(this, ReservationListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        String content = "Teren: " + fieldName
                + " | " + lastReservationDate
                + " " + lastReservationStartTime + "-" + lastReservationEndTime
                + " | Cena: " + String.format("%.0f", lastCalculatedPrice) + " RSD";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Nova rezervacija")
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_up);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}
