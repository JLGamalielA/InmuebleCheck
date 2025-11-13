package com.example.inmueblecheck;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
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


    public void loadInspeccionDetalle(String inspectionId) {
        if (inspectionId == null) {
            Log.e(TAG, "ID de inspección es nulo");
            return;
        }

        db.collection("inspecciones").document(inspectionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Inspeccion insp = documentSnapshot.toObject(Inspeccion.class);
                        inspeccionData.setValue(insp);

                        // 2. Cargar el mapa del checklist
                        if (documentSnapshot.contains("checklist")) {
                            Map<String, Map<String, Object>> checklist = (Map<String, Map<String, Object>>) documentSnapshot.get("checklist");
                            checklistData.setValue(checklist);
                        }
                    } else {
                        Log.e(TAG, "No se encontró el documento: " + inspectionId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al cargar inspección", e));

        StorageReference mediaRef = storage.getReference()
                .child("inspecciones")
                .child(inspectionId);

        mediaRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<Media> mediaList = new ArrayList<>();
                    for (StorageReference itemRef : listResult.getItems()) {
                        // Para cada item, se obtiene su URL de descarga
                        itemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Media media = new Media();
                            media.setRemoteUri(uri.toString());
                            media.setItemName(itemRef.getName()); // Nombre del archivo

                            // Determinamos el tipo por el nombre
                            if (itemRef.getName().contains("JPEG_") || itemRef.getName().contains(".jpg")) {
                                media.setType("image");
                            } else if (itemRef.getName().contains("MP4_") || itemRef.getName().contains(".mp4")) {
                                media.setType("video");
                            } else {
                                media.setType("file");
                            }

                            mediaList.add(media);
                            // Actualiza el LiveData cada vez que se añade una URL
                            mediaData.setValue(new ArrayList<>(mediaList)); // Crea una nueva lista para notificar al observador
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al listar media", e));
    }
}