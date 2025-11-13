package com.example.inmueblecheck;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "media_table")
public class Media {

    @PrimaryKey
    @NonNull
    private String mediaId;
    private String inspectionId;
    private String itemName;
    private String localUri; // Dónde está en el teléfono
    private String remoteUri; // Dónde se subió en Firebase Storage

    private String type; // "image" o "video"
    private boolean isSynced;

    public Media() {
        this.isSynced = false;
    }

    @NonNull
    public String getMediaId() { return mediaId; }
    public void setMediaId(@NonNull String mediaId) { this.mediaId = mediaId; }
    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getLocalUri() { return localUri; }
    public void setLocalUri(String localUri) { this.localUri = localUri; }
    public String getRemoteUri() { return remoteUri; }
    public void setRemoteUri(String remoteUri) { this.remoteUri = remoteUri; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }
}