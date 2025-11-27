package com.example.inmueblecheck;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GerenteDetalleViewModel extends ViewModel {

    private static final String TAG = "GerenteDetalleVM";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private MutableLiveData<Inspeccion> inspeccionData = new MutableLiveData<>();
    private MutableLiveData<Map<String, Map<String, Object>>> checklistData = new MutableLiveData<>();
    private MutableLiveData<List<Media>> mediaData = new MutableLiveData<>();

    public LiveData<Inspeccion> getInspeccion() { return inspeccionData; }
    public LiveData<Map<String, Map<String, Object>>> getChecklist() { return checklistData; }
    public LiveData<List<Media>> getMediaList() { return mediaData; }

    /**
     * Carga todos los detalles de una inspección (datos, checklist y galería)
     * desde Firebase.
     */
    public void loadInspeccionDetalle(String inspectionId) {

        // Validación de entrada
        if (inspectionId == null) {
            Log.e(TAG, "ID de inspección es nulo");
            return; // Fin de la ejecución si no hay ID
        }

        // Cargar Datos de la Inspección (Dirección, GPS, Estado)
        db.collection("inspecciones").document(inspectionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Inspeccion insp = documentSnapshot.toObject(Inspeccion.class);
                        inspeccionData.setValue(insp);

                        // Cargar el Checklist (Versión Segura)
                        if (documentSnapshot.contains("checklist")) {
                            Object rawChecklistObject = documentSnapshot.get("checklist");

                            if (rawChecklistObject instanceof Map) {
                                Map<String, Object> rawMap = (Map<String, Object>) rawChecklistObject;
                                Map<String, Map<String, Object>> safeChecklistMap = new HashMap<>();

                                try {
                                    for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                                        if (entry.getValue() instanceof Map) {
                                            safeChecklistMap.put(entry.getKey(), (Map<String, Object>) entry.getValue());
                                        }
                                    }
                                    checklistData.setValue(safeChecklistMap);

                                } catch (ClassCastException e) {
                                    Log.e(TAG, "Error de casteo al procesar el checklist de Firestore", e);
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "No se encontró el documento: " + inspectionId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al cargar inspección", e));

        // Cargar Galería de Medios (Versión Eficiente)
        StorageReference mediaRef = storage.getReference()
                .child("inspecciones")
                .child(inspectionId);

        mediaRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (listResult.getItems().isEmpty()) {
                        mediaData.setValue(new ArrayList<>()); // Enviar lista vacía si no hay media
                        return;
                    }

                    List<Task<Uri>> tasks = new ArrayList<>();
                    List<StorageReference> itemRefs = listResult.getItems();

                    // Añade todas las tareas de obtención de URL a una lista
                    for (StorageReference itemRef : itemRefs) {
                        tasks.add(itemRef.getDownloadUrl());
                    }

                    // Espera a que TODAS las tareas terminen
                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(uris -> {
                        List<Media> mediaList = new ArrayList<>();

                        for (int i = 0; i < uris.size(); i++) {
                            Uri uri = (Uri) uris.get(i);
                            StorageReference itemRef = itemRefs.get(i); // Obtener la referencia original

                            Media media = new Media();
                            media.setRemoteUri(uri.toString());
                            media.setItemName(itemRef.getName());

                            String itemName = itemRef.getName(); // Usar variable local

                            // Determinamos el tipo por el nombre
                            if (itemName.contains("JPEG_") || itemName.contains(".jpg")) {
                                media.setType("image");
                            } else if (itemName.contains("MP4_") || itemName.contains(".mp4")) { // Typo corregido
                                media.setType("video");
                            } else {
                                media.setType("file");
                            }
                            mediaList.add(media);
                        }

                        // Actualiza el LiveData UNA SOLA VEZ con la lista completa
                        mediaData.setValue(mediaList);
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al listar media", e));
    }
}