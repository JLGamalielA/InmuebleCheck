package com.example.inmueblecheck;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private final InspeccionDao dao;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // Inicializar
        AppDatabase appDb = AppDatabase.getDatabase(context);
        dao = appDb.inspeccionDao();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker: El trabajo ha comenzado.");

        try {
            // Obtener todas las inspecciones pendientes de sincronizar de Room
            List<Inspeccion> inspectionsToSync = dao.getPendingInspectionsToSync();
            if (inspectionsToSync.isEmpty()) {
                Log.d(TAG, "SyncWorker: No hay inspecciones pendientes.");
                return Result.success();
            }

            for (Inspeccion insp : inspectionsToSync) {
                if ("pendiente_sync".equals(insp.getStatus())) {
                    Log.d(TAG, "SyncWorker: Sincronizando inspección: " + insp.getDocumentId());

                    // Subir Media (Fotos/Videos) a Firebase Storage
                    uploadMedia(insp.getDocumentId());

                    // Subir datos del Checklist a Firestore
                    uploadChecklistData(insp);
                }
            }
            Log.d(TAG, "SyncWorker: Trabajo finalizado exitosamente.");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "SyncWorker: Error durante la sincronización.", e);
            return Result.retry(); // Reintentar más tarde
        }
    }

    private void uploadMedia(String inspectionId) throws ExecutionException, InterruptedException {
        List<Media> mediaToSync = dao.getMediaToSync(inspectionId);
        if (mediaToSync.isEmpty()) {
            Log.d(TAG, "No hay media para sincronizar para: " + inspectionId);
            return;
        }

        Log.d(TAG, "Subiendo " + mediaToSync.size() + " archivos de media...");

        for (Media media : mediaToSync) {
            Uri fileUri = Uri.parse(media.getLocalUri());
            StorageReference storageRef = storage.getReference()
                    .child("inspecciones")
                    .child(inspectionId)
                    .child(new File(fileUri.getPath()).getName());

            UploadTask uploadTask = storageRef.putFile(fileUri);
            Task<Uri> getUrlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    // Si la subida falla, lanza una excepción
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            });

            Uri downloadUri = Tasks.await(getUrlTask);
            media.setRemoteUri(downloadUri.toString());
            media.setSynced(true);
            dao.updateMedia(media); // Actualizar en Room
            Log.d(TAG, "Archivo subido: " + downloadUri.toString());
        }
    }

    private void uploadChecklistData(Inspeccion inspection) throws ExecutionException, InterruptedException {
        List<ChecklistItem> checklistItems = dao.getChecklistItemsToSync(inspection.getDocumentId());

        Map<String, Object> inspectionData = new HashMap<>();
        inspectionData.put("status", "completada"); // Actualizar estado
        inspectionData.put("latitud", inspection.getLatitud());
        inspectionData.put("longitud", inspection.getLongitud());

        // Convertir lista de checklist a un formato que Firestore entienda
        Map<String, Object> checklistMap = new HashMap<>();
        for (ChecklistItem item : checklistItems) {
            // Guardamos un "mapa" de objetos, no solo strings
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("notes", item.getNotes());
            itemData.put("isCompleted", true); // (O el estado que tengas)
            checklistMap.put(item.getItemName(), itemData);
        }
        inspectionData.put("checklist", checklistMap);

        // Subir datos (síncrono)
        Task<Void> updateTask = db.collection("inspecciones")
                .document(inspection.getDocumentId())
                .update(inspectionData);

        Tasks.await(updateTask); // Espera a que termine
        if (updateTask.isSuccessful()) {
            dao.updateInspectionStatus(inspection.getDocumentId(), "completada");
            Log.d(TAG, "Datos de checklist subidos para: " + inspection.getDocumentId());
        } else {
            Log.e(TAG, "Error subiendo checklist", updateTask.getException());
        }
    }
}