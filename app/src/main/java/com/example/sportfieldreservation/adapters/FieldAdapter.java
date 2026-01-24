package com.example.sportfieldreservation.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportfieldreservation.R;
import com.example.sportfieldreservation.activities.FieldDetailActivity;
import com.example.sportfieldreservation.model.Field;

import java.util.ArrayList;
import java.util.List;

public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.FieldViewHolder> {

    public interface OnFieldClickListener {
        void onFieldClick(Field field);
    }

    private List<Field> fields = new ArrayList<>();
    private OnFieldClickListener clickListener;

    public FieldAdapter(List<Field> fields) {
        if (fields != null) {
            this.fields = fields;
        }
    }

    public void setFields(List<Field> fields) {
        this.fields = (fields != null) ? fields : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnFieldClickListener(OnFieldClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_field, parent, false);
        return new FieldViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        Field field = fields.get(position);
        holder.bind(field);
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    class FieldViewHolder extends RecyclerView.ViewHolder {

        TextView tvFieldName;
        TextView tvFieldType;
        TextView tvFieldLocation;
        TextView tvFieldPrice;

        public FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldType = itemView.findViewById(R.id.tvFieldType);
            tvFieldLocation = itemView.findViewById(R.id.tvFieldLocation);
            tvFieldPrice = itemView.findViewById(R.id.tvFieldPrice);
        }

        public void bind(Field field) {
            tvFieldName.setText(field.getName());

            String type = field.getType();
            if (type == null || type.trim().isEmpty()) {
                type = "Nepoznat tip";
            }
            tvFieldType.setText("Tip: " + type);

            String location = field.getLocation();
            if (location == null || location.trim().isEmpty()) {
                location = "Nepoznata lokacija";
            }
            tvFieldLocation.setText("Lokacija: " + location);

            tvFieldPrice.setText("Cena: " + field.getPricePerHour() + " RSD/h");

            itemView.setOnClickListener(v -> {
                // Ako nam je neko setovao listener (tablet/telefon preko Activity-ja) → javljamo njemu
                if (clickListener != null) {
                    clickListener.onFieldClick(field);
                    return;
                }

                // Inače (fallback) ponašaj se kao ranije i otvori FieldDetailActivity direktno
                Context context = itemView.getContext();
                Intent intent = new Intent(context, FieldDetailActivity.class);
                intent.putExtra("field_name", field.getName());
                intent.putExtra("field_type", field.getType());
                intent.putExtra("field_location", field.getLocation());
                intent.putExtra("field_price", field.getPricePerHour());
                context.startActivity(intent);
            });
        }
    }
}
