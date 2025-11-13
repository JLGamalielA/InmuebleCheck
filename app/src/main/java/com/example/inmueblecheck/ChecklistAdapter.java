package com.example.inmueblecheck;

import android.app.Activity;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistItemViewHolder> {

    private List<ChecklistItem> checklist = new ArrayList<>();
    private ChecklistItemListener listener;

    public interface ChecklistItemListener {
        void onCameraClick(String itemName);
        void onVideoClick(String itemName);
        void onNotesChanged(String itemName, String notes);
    }

    public ChecklistAdapter(ChecklistItemListener listener)
    {
        this.listener = listener;
    }

    public void setChecklist(List<ChecklistItem> checklist)
    {
        this.checklist = checklist;
        notifyDataSetChanged();
    }

    public List<ChecklistItem> getItems() {
        return checklist;
    }

    @NonNull
    @Override
    public ChecklistItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist, parent, false);
        return new ChecklistItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistItemViewHolder holder, int position)
    {
        ChecklistItem item = checklist.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount()
    {
        return checklist.size();
    }


    public void updateNotesForItem(String itemName, String notes)
    {
        if (checklist == null) return;

        for (ChecklistItem item : checklist)
        {
            if (item.getItemName().equals(itemName))
            {
                item.setNotes(notes);

                Log.d("ChecklistAdapter", "Notas actualizadas para " + itemName + ": " + notes);
                return;
            }
        }
    }


    // --- ViewHolder Interno ---
    static class ChecklistItemViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemName;
        private Button btnCamera, btnVideo;
        private EditText editTextNotes;
        private TextInputLayout tilNotes;
        private boolean isBinding = false; // Flag para evitar bucles en TextWatcher

        public ChecklistItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            btnCamera = itemView.findViewById(R.id.btnCamera);
            btnVideo = itemView.findViewById(R.id.btnVideo);
            editTextNotes = itemView.findViewById(R.id.etNotes);
            tilNotes = itemView.findViewById(R.id.tilNotes);
        }

        public void bind(ChecklistItem item, ChecklistItemListener listener) {
            tvItemName.setText(item.getItemName());
            TextWatcher oldWatcher = (TextWatcher) editTextNotes.getTag();
            if (oldWatcher != null) {
                editTextNotes.removeTextChangedListener(oldWatcher);
            }

            isBinding = true; // Activar flag
            editTextNotes.setText(item.getNotes());
            isBinding = false; // Desactivar flag

            TextWatcher newWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (isBinding) return;

                    if (listener != null) {
                        listener.onNotesChanged(item.getItemName(), s.toString());
                    }
                }
            };
            editTextNotes.addTextChangedListener(newWatcher);
            editTextNotes.setTag(newWatcher); // Guardar el listener para poder quitarlo luego

            btnCamera.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCameraClick(item.getItemName());
                }
            });

            btnVideo.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoClick(item.getItemName());
                }
            });
        }
    }
}