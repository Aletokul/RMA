package com.example.sportfieldreservation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportfieldreservation.R;
import com.example.sportfieldreservation.adapters.FieldAdapter;
import com.example.sportfieldreservation.model.Field;
import com.example.sportfieldreservation.network.FieldApiService;
import com.example.sportfieldreservation.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FieldListActivity extends AppCompatActivity {

    // Lista (uvek desno, i na telefonu i na tabletu)
    private RecyclerView rvFields;
    private FieldAdapter fieldAdapter;

    // Filter polja (uvek postoje)
    private EditText etFieldFilter;
    private Button btnApplyFieldFilter, btnClearFieldFilter;

    // Tablet detalji (postoje samo u layout-sw600dp)
    private TextView tvDetailFieldName;
    private TextView tvDetailFieldType;
    private TextView tvDetailFieldLocation;
    private TextView tvDetailFieldPrice;
    private TextView tvDetailPlaceholder;
    private Button btnReserveFieldTablet;

    private boolean isTabletLayout = false;

    private List<Field> allFields = new ArrayList<>();
    private Field selectedFieldForTablet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_list);

        // Glavna lista
        rvFields = findViewById(R.id.rvFields);
        rvFields.setLayoutManager(new LinearLayoutManager(this));
        fieldAdapter = new FieldAdapter(new ArrayList<>());
        rvFields.setAdapter(fieldAdapter);

        // Filter (isti ID-jevi i za telefon i za tablet)
        etFieldFilter = findViewById(R.id.etFieldFilter);
        btnApplyFieldFilter = findViewById(R.id.btnApplyFieldFilter);
        btnClearFieldFilter = findViewById(R.id.btnClearFieldFilter);

        setupFilterButtons();

        // Provera da li postoje detalji za tablet (ako postoje → layout-sw600dp)
        tvDetailFieldName = findViewById(R.id.tvDetailFieldName);
        tvDetailFieldType = findViewById(R.id.tvDetailFieldType);
        tvDetailFieldLocation = findViewById(R.id.tvDetailFieldLocation);
        tvDetailFieldPrice = findViewById(R.id.tvDetailFieldPrice);
        tvDetailPlaceholder = findViewById(R.id.tvDetailPlaceholder);
        btnReserveFieldTablet = findViewById(R.id.btnReserveFieldTablet);

        isTabletLayout = (tvDetailFieldName != null && btnReserveFieldTablet != null);

        // Klik na teren
        fieldAdapter.setOnFieldClickListener(field -> {
            if (isTabletLayout) {
                // Tablet: popuni levi panel
                selectedFieldForTablet = field;
                showFieldDetailsOnTablet(field);
            } else {
                // Telefon: otvori FieldDetailActivity kao i ranije
                Intent intent = new Intent(FieldListActivity.this, FieldDetailActivity.class);
                intent.putExtra("field_name", field.getName());
                intent.putExtra("field_type", field.getType());
                intent.putExtra("field_location", field.getLocation());
                intent.putExtra("field_price", field.getPricePerHour());
                startActivity(intent);
            }
        });

        // TABLET: dugme "Rezerviši"
        if (isTabletLayout && btnReserveFieldTablet != null) {
            btnReserveFieldTablet.setOnClickListener(v -> {
                if (selectedFieldForTablet == null) {
                    Toast.makeText(FieldListActivity.this,
                            "Prvo izaberi teren sa desne strane.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent reserveIntent = new Intent(FieldListActivity.this, AddReservationActivity.class);
                reserveIntent.putExtra("field_name", selectedFieldForTablet.getName());
                reserveIntent.putExtra("field_price_per_hour", selectedFieldForTablet.getPricePerHour());
                startActivity(reserveIntent);
                overridePendingTransition(R.anim.slide_in_down, 0);
            });
        }

        // TABLET: klik na lokaciju → otvori mape
        if (isTabletLayout && tvDetailFieldLocation != null) {
            tvDetailFieldLocation.setOnClickListener(v -> openFieldLocationInMaps());
        }

        loadFieldsFromApi();
    }

    private void setupFilterButtons() {
        if (btnApplyFieldFilter != null) {
            btnApplyFieldFilter.setOnClickListener(v -> applyFieldFilter());
        }
        if (btnClearFieldFilter != null) {
            btnClearFieldFilter.setOnClickListener(v -> clearFieldFilter());
        }
    }

    private void loadFieldsFromApi() {
        FieldApiService apiService = RetrofitClient.getInstance().create(FieldApiService.class);

        Call<List<Field>> call = apiService.getFields();
        call.enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFields = response.body();
                    fieldAdapter.setFields(allFields);
                } else {
                    Toast.makeText(FieldListActivity.this,
                            "Greška pri učitavanju terena",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                Toast.makeText(FieldListActivity.this,
                        "Neuspešan zahtev: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFieldFilter() {
        if (etFieldFilter == null) return;

        String query = etFieldFilter.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            // nema filtera → prikaži sve
            fieldAdapter.setFields(allFields);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Field> filtered = new ArrayList<>();

        for (Field f : allFields) {
            String name = f.getName() != null ? f.getName().toLowerCase() : "";
            String type = f.getType() != null ? f.getType().toLowerCase() : "";

            if (name.contains(lowerQuery) || type.contains(lowerQuery)) {
                filtered.add(f);
            }
        }

        fieldAdapter.setFields(filtered);
    }

    private void clearFieldFilter() {
        if (etFieldFilter != null) {
            etFieldFilter.setText("");
        }
        fieldAdapter.setFields(allFields);
    }

    // Popunjavanje levog panela na tabletu
    private void showFieldDetailsOnTablet(Field field) {
        if (!isTabletLayout || field == null) return;

        if (tvDetailPlaceholder != null) {
            tvDetailPlaceholder.setVisibility(View.GONE);
        }

        if (tvDetailFieldName != null) {
            String name = field.getName();
            if (name == null || name.trim().isEmpty()) {
                name = "Nepoznat teren";
            }
            tvDetailFieldName.setText(name);
            tvDetailFieldName.setVisibility(View.VISIBLE);
        }

        if (tvDetailFieldType != null) {
            String type = field.getType();
            if (type == null || type.trim().isEmpty()) {
                tvDetailFieldType.setText("Tip: -");
            } else {
                tvDetailFieldType.setText("Tip: " + type);
            }
            tvDetailFieldType.setVisibility(View.VISIBLE);
        }

        if (tvDetailFieldLocation != null) {
            String location = field.getLocation();
            if (location == null || location.trim().isEmpty()) {
                tvDetailFieldLocation.setText("Lokacija: -");
            } else {
                tvDetailFieldLocation.setText("Lokacija: " + location);
            }
            tvDetailFieldLocation.setVisibility(View.VISIBLE);
        }

        if (tvDetailFieldPrice != null) {
            int price = field.getPricePerHour();
            if (price > 0) {
                tvDetailFieldPrice.setText("Cena: " + price + " RSD/h");
            } else {
                tvDetailFieldPrice.setText("Cena: -");
            }
            tvDetailFieldPrice.setVisibility(View.VISIBLE);
        }
    }

    // 👉 OTVARANJE LOKACIJE NA MAPAMA NA TABLETU
    private void openFieldLocationInMaps() {
        if (!isTabletLayout || selectedFieldForTablet == null) return;

        String fieldName = selectedFieldForTablet.getName();
        String fieldLocation = selectedFieldForTablet.getLocation();

        if (fieldLocation == null || fieldLocation.trim().isEmpty()) {
            Toast.makeText(this, "Lokacija nije dostupna.", Toast.LENGTH_SHORT).show();
            return;
        }

        // isti fazon kao u FieldDetailActivity
        String query;
        if (fieldName != null && !fieldName.trim().isEmpty() && !"Nepoznat teren".equals(fieldName)) {
            query = fieldName + " " + fieldLocation; // npr. "Balon Detelinara Novi Sad"
        } else {
            query = fieldLocation; // npr. "Novi Sad"
        }

        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Nije moguće otvoriti mapu.", Toast.LENGTH_SHORT).show();
        }
    }
}
