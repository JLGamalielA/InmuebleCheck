package com.example.inmueblecheck;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.Exclude;

@Entity(tableName = "checklist_table")
public class ChecklistItem {

    @PrimaryKey
    @NonNull
    private String itemId;
    private String inspectionId;
    private String itemName;
    private String notes;
    private boolean isCompleted;

    // --- Constructores ---
    public ChecklistItem() {
        this.isCompleted = false;
    }

    @Ignore // Ignoramos este constructor para Room
    public ChecklistItem(String inspectionId, String itemName, String notes) {
        this.itemId = java.util.UUID.randomUUID().toString();
        this.inspectionId = inspectionId;
        this.itemName = itemName;
        this.notes = notes;
        this.isCompleted = false;
    }

    // --- Getters y Setters ---
    @NonNull
    public String getItemId() { return itemId; }
    public void setItemId(@NonNull String itemId) { this.itemId = itemId; }
    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Exclude
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}