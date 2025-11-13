package com.example.inmueblecheck;

import android.app.Application;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.UUID;

public class DetalleInspeccionViewModel extends AndroidViewModel {
    private static final String TAG = "DetalleViewModel";
    private InspeccionRepository repository;
    private LiveData<List<ChecklistItem>> checklist;
    private LiveData<List<Media>> mediaList;
    private MutableLiveData<Boolean> saveStatus = new MutableLiveData<>();

    public DetalleInspeccionViewModel(@NonNull Application application) {
        super(application);
        repository = new InspeccionRepository(application);
    }

    public void loadChecklist(String inspectionId) {
        if (checklist == null) {
            checklist = repository.getChecklistForInspection(inspectionId);
            mediaList = repository.getMediaForInspection(inspectionId);
        }

        // Pre-carga el checklist si está vacío
        repository.createInitialChecklist(inspectionId);
    }

    public LiveData<List<ChecklistItem>> getChecklist() {
        return checklist;
    }

    public LiveData<Boolean> getSaveStatus() {
        return saveStatus;
    }

    public void saveOfflineEvidence(String inspectionId, List<ChecklistItem> items, double latitude, double longitude) {
        Log.d(TAG, "Guardando evidencia para: " + inspectionId);
        repository.saveChecklistAndFinalizeInspection(inspectionId, items, latitude, longitude);
        saveStatus.postValue(true); // Notifica al Fragment que se guardó
    }

    public void saveMedia(String inspectionId, String itemName, String uri, String type) {
        Log.d(TAG, "Guardando media: " + type + " para " + itemName + " en " + uri);

        Media newMedia = new Media();
        newMedia.setMediaId(UUID.randomUUID().toString()); // ID único para el archivo
        newMedia.setInspectionId(inspectionId);
        newMedia.setItemName(itemName);
        newMedia.setLocalUri(uri);
        newMedia.setType(type);
        newMedia.setSynced(false);

        repository.insertMedia(newMedia);
    }

    public LiveData<List<Media>> getMediaList() {
        return mediaList;
    }
}