package com.example.inmueblecheck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;

public class InspeccionAdapter extends RecyclerView.Adapter<InspeccionAdapter.InspeccionViewHolder> {

    private List<Inspeccion> listaInspecciones = new ArrayList<>();

    public interface OnInspeccionClickListener {
        void onInspeccionClick(Inspeccion inspeccion);
    }

    private OnInspeccionClickListener clickListener;

    public void setOnInspeccionClickListener(OnInspeccionClickListener listener) {
        this.clickListener = listener;
    }

    public void setInspecciones(List<Inspeccion> inspecciones) {
        this.listaInspecciones = inspecciones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InspeccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inspeccion, parent, false);
        return new InspeccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InspeccionViewHolder holder, int position) {
        Inspeccion inspeccion = listaInspecciones.get(position);
        holder.bind(inspeccion, clickListener);
    }

    @Override
    public int getItemCount() {
        return listaInspecciones.size();
    }

    static class InspeccionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDireccion, tvAgenteEmail, tvStatus;

        public InspeccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvAgenteEmail = itemView.findViewById(R.id.tvAgenteEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Inspeccion inspeccion, OnInspeccionClickListener listener) {
            tvDireccion.setText(inspeccion.getDireccion());
            tvAgenteEmail.setText(inspeccion.getAgentEmail());

            String statusText = "Estado: " + inspeccion.getStatus();
            tvStatus.setText(statusText);

            int color;
            if ("pendiente".equals(inspeccion.getStatus())) {
                color = itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
            } else if ("completada".equals(inspeccion.getStatus())) {
                color = itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
            } else { // "pendiente_sync"
                color = itemView.getContext().getResources().getColor(android.R.color.holo_blue_light);
            }
            tvStatus.setTextColor(color);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInspeccionClick(inspeccion);
                }
            });
        }
    }
}