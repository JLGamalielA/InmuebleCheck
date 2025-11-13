package com.example.inmueblecheck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChecklistReadOnlyAdapter extends RecyclerView.Adapter<ChecklistReadOnlyAdapter.ViewHolder> {

    private List<Map.Entry<String, Map<String, Object>>> checklistItems = new ArrayList<>();

    // Recibe el Mapa de Firestore
    public void setChecklistMap(Map<String, Map<String, Object>> checklistMap) {
        this.checklistItems = new ArrayList<>(checklistMap.entrySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist_readonly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Map<String, Object>> entry = checklistItems.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return checklistItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName;
        private TextView tvItemNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemNameReadOnly);
            tvItemNotes = itemView.findViewById(R.id.tvItemNotesReadOnly);
        }

        public void bind(Map.Entry<String, Map<String, Object>> entry) {
            String itemName = entry.getKey();
            String notes = "Sin notas."; // Default

            // El valor es otro Mapa
            if (entry.getValue() != null && entry.getValue().get("notes") != null) {
                notes = entry.getValue().get("notes").toString();
            }

            tvItemName.setText(itemName);
            tvItemNotes.setText(notes);
        }
    }
}