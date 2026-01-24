package com.example.sportfieldreservation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportfieldreservation.R;
import com.example.sportfieldreservation.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnReservationLongClickListener {
        void onReservationLongClick(Reservation reservation);
    }

    private List<Reservation> reservations = new ArrayList<>();
    private OnReservationLongClickListener longClickListener;

    public ReservationAdapter(List<Reservation> reservations,
                              OnReservationLongClickListener longClickListener) {
        if (reservations != null) {
            this.reservations = reservations;
        }
        this.longClickListener = longClickListener;
    }

    public void setReservations(List<Reservation> newReservations) {
        this.reservations = (newReservations != null) ? newReservations : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.bind(reservation, longClickListener);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {

        TextView tvFieldName;
        TextView tvDateTime;
        TextView tvPrice;
        TextView tvNote;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvItemReservationFieldName);
            tvDateTime = itemView.findViewById(R.id.tvItemReservationDateTime);
            tvPrice = itemView.findViewById(R.id.tvItemReservationPrice);
            tvNote = itemView.findViewById(R.id.tvItemReservationNote);
        }

        public void bind(Reservation reservation,
                         OnReservationLongClickListener longClickListener) {

            // Naziv terena
            String fieldName = reservation.getFieldName();
            if (fieldName == null || fieldName.trim().isEmpty()) {
                fieldName = "Nepoznat teren";
            }
            tvFieldName.setText(fieldName);

            // Datum + vreme
            String date = reservation.getDate();
            String start = reservation.getStartTime();
            String end = reservation.getEndTime();

            StringBuilder dateTimeText = new StringBuilder();
            if (date != null && !date.trim().isEmpty()) {
                dateTimeText.append(date);
            }
            if (start != null && !start.trim().isEmpty()) {
                if (dateTimeText.length() > 0) dateTimeText.append("  •  ");
                dateTimeText.append(start);
            }
            if (end != null && !end.trim().isEmpty()) {
                dateTimeText.append(" - ").append(end);
            }
            if (dateTimeText.length() == 0) {
                dateTimeText.append("Datum i vreme nisu postavljeni");
            }
            tvDateTime.setText(dateTimeText.toString());

            // Cena
            double total = reservation.getTotalPrice();
            if (total > 0) {
                tvPrice.setText("Cena: " + String.format("%.0f", total) + " RSD");
            } else {
                tvPrice.setText("Cena: -");
            }

            // Napomena
            String note = reservation.getNote();
            if (note == null || note.trim().isEmpty()) {
                tvNote.setText("Bez napomene");
            } else {
                tvNote.setText(note);
            }

            // Long click za brisanje
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onReservationLongClick(reservation);
                }
                return true;
            });
        }
    }
}
