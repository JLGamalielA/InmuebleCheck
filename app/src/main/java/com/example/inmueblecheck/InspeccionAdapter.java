package com.example.inmueblecheck;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inmueblecheck.Inspeccion;

import java.util.ArrayList;
import java.util.List;

public class InspeccionAdapter extends RecyclerView.Adapter<InspeccionAdapter.InspeccionViewHolder> {

    private List<Inspeccion> listaInspecciones = new ArrayList<>();
    private OnInspeccionClickListener clickListener;

    public interface OnInspeccionClickListener {
        void onInspeccionClick(Inspeccion inspeccion);
    }

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
        return listaInspecciones != null ? listaInspecciones.size() : 0;
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
            Context context = itemView.getContext();

            // Dirección
            tvDireccion.setText(inspeccion.getDireccion() != null ? inspeccion.getDireccion() : "Sin Dirección");

            // Email del Agente
            String email = inspeccion.getAgentEmail();
            tvAgenteEmail.setText((email != null && !email.isEmpty()) ? email : "Sin agente asignado");
            String estado = inspeccion.getStatus();
            if (estado == null || estado.isEmpty()) estado = "pendiente";

            try {
                String estadoFormato = estado.substring(0, 1).toUpperCase() + estado.substring(1);
                tvStatus.setText(estadoFormato);
            } catch (Exception e) {
                tvStatus.setText(estado);
            }

            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.bg_status_pending);
            if (background != null) {
                background = (GradientDrawable) background.getConstantState().newDrawable().mutate();
                if ("completada".equalsIgnoreCase(estado)) {
                    background.setColor(Color.parseColor("#E8F5E9"));
                    tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                } else {
                    background.setColor(Color.parseColor("#FFF3E0"));
                    tvStatus.setTextColor(Color.parseColor("#F57C00"));
                }
                tvStatus.setBackground(background);
            }

            // Listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInspeccionClick(inspeccion);
                }
            });
        }
    }
}