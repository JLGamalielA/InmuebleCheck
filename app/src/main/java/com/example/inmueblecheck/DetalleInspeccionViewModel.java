package com.example.inmueblecheck;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DetalleInspeccionViewModel extends AndroidViewModel {
    private static final String TAG = "DetalleViewModel";
    private InspeccionRepository repository;
    private MutableLiveData<String> inspectionIdLiveData = new MutableLiveData<>();
    private LiveData<List<ChecklistItem>> checklist;
    private LiveData<List<Media>> mediaList;
    private MutableLiveData<Boolean> saveStatus = new MutableLiveData<>();

    public DetalleInspeccionViewModel(@NonNull Application application) {
        super(application);
        repository = new InspeccionRepository(application);

        checklist = Transformations.switchMap(inspectionIdLiveData, inspectionId -> {
            if (inspectionId == null || inspectionId.isEmpty()) {
                Log.w(TAG, "inspectionId es nulo o vacío");
                MutableLiveData<List<ChecklistItem>> emptyLiveData = new MutableLiveData<>();
                emptyLiveData.setValue(new ArrayList<>());
                return emptyLiveData;
            }
            Log.d(TAG, "Cargando checklist para: " + inspectionId);
            return repository.getChecklistForInspection(inspectionId);
        });

        mediaList = Transformations.switchMap(inspectionIdLiveData, inspectionId -> {
            if (inspectionId == null || inspectionId.isEmpty()) {
                MutableLiveData<List<Media>> emptyLiveData = new MutableLiveData<>();
                emptyLiveData.setValue(new ArrayList<>());
                return emptyLiveData;
            }
            return repository.getMediaForInspection(inspectionId);
        });
    }

    public void loadChecklist(String inspectionId) {
        if (inspectionId == null || inspectionId.isEmpty()) {
            Log.e(TAG, "inspectionId es nulo o vacío en loadChecklist");
            return;
        }

        Log.d(TAG, "loadChecklist iniciado para: " + inspectionId);

        inspectionIdLiveData.setValue(inspectionId);
        repository.createInitialChecklist(inspectionId);
    }

    public LiveData<List<ChecklistItem>> getChecklist() {
        Log.d(TAG, "getChecklist() llamado");
        return checklist != null ? checklist : new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<Boolean> getSaveStatus() {
        return saveStatus;
    }

    public void saveOfflineEvidence(String inspectionId, List<ChecklistItem> items, double latitude, double longitude) {
        if (inspectionId == null || items == null) {
            Log.e(TAG, "inspectionId o items son nulos");
            saveStatus.postValue(false);
            return;
        }

        Log.d(TAG, "Guardando evidencia para: " + inspectionId + " con " + items.size() + " items");

        try {
            repository.saveChecklistAndFinalizeInspection(inspectionId, items, latitude, longitude);
            saveStatus.postValue(true);
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar evidencia", e);
            saveStatus.postValue(false);
        }
    }

    public void saveMedia(String inspectionId, String itemName, String uri, String type) {
        if (inspectionId == null || itemName == null || uri == null) {
            Log.e(TAG, "Parámetros nulos: inspectionId=" + inspectionId + ", itemName=" + itemName + ", uri=" + uri);
            return;
        }

        Log.d(TAG, "Guardando media: " + type + " para " + itemName + " en " + uri);

        try {
            Media newMedia = new Media();
            newMedia.setMediaId(UUID.randomUUID().toString());
            newMedia.setInspectionId(inspectionId);
            newMedia.setItemName(itemName);
            newMedia.setLocalUri(uri);
            newMedia.setType(type);
            newMedia.setSynced(false);

            repository.insertMedia(newMedia);
            Log.d(TAG, "Media insertada correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar media", e);
        }
    }

    public LiveData<List<Media>> getMediaList() {
        return mediaList != null ? mediaList : new MutableLiveData<>(new ArrayList<>());
    }
}