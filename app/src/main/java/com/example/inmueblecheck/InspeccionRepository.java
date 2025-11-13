package com.example.inmueblecheck;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InspeccionRepository {

    private static final String TAG = "InspeccionRepository";
    private final InspeccionDao inspeccionDao;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final WorkManager workManager;
    private final ExecutorService executor;

    public InspeccionRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        inspeccionDao = database.inspeccionDao();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        workManager = WorkManager.getInstance(application);
        executor = Executors.newSingleThreadExecutor();
    }

    // Para el Dashboard del Agente
    public LiveData<List<Inspeccion>> getAllInspecciones() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Sincroniza desde Firestore (asíncrono)
            fetchInspectionsFromFirestore(user.getUid());
        }
        // Devuelve INMEDIATAMENTE los datos locales de Room
        return inspeccionDao.getAllInspecciones();
    }

    // Trae de Firestore y guarda en Room
    private void fetchInspectionsFromFirestore(String agentId) {
        db.collection("inspecciones")
                .whereEqualTo("agentId", agentId)
                //.whereEqualTo("status", "pendiente") // Comentado para que sincronice todos
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Inspeccion> inspections = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Inspeccion insp = doc.toObject(Inspeccion.class);
                        if (insp != null) {
                            insp.setDocumentId(doc.getId());
                            inspections.add(insp);
                        }
                    }
                    executor.execute(() -> inspeccionDao.insertAll(inspections));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching from Firestore", e));
    }


    public LiveData<List<ChecklistItem>> getChecklistForInspection(String inspectionId) {
        return inspeccionDao.getChecklistForInspection(inspectionId);
    }

    public LiveData<List<Media>> getMediaForInspection(String inspectionId) {
        return inspeccionDao.getMediaForInspection(inspectionId);
    }

    public void insertMedia(Media media) {
        executor.execute(() -> inspeccionDao.insertMedia(media));
    }

    // Crea el checklist inicial si no existe
    public void createInitialChecklist(String inspectionId) {
        executor.execute(() -> {
            List<ChecklistItem> items = inspeccionDao.getChecklistItemsToSync(inspectionId);
            if (items == null || items.isEmpty()) {
                inspeccionDao.insertChecklistItem(new ChecklistItem(inspectionId, "Cocina", ""));
                inspeccionDao.insertChecklistItem(new ChecklistItem(inspectionId, "Baño 1", ""));
                inspeccionDao.insertChecklistItem(new ChecklistItem(inspectionId, "Recámara 1", ""));
                inspeccionDao.insertChecklistItem(new ChecklistItem(inspectionId, "Estancia", ""));
                inspeccionDao.insertChecklistItem(new ChecklistItem(inspectionId, "Fachada", ""));
            }
        });
    }

    public void saveChecklistAndFinalizeInspection(String inspectionId, List<ChecklistItem> items, double latitude, double longitude) {
        executor.execute(() -> { // <-- Inicia el hilo de fondo

            // Guarda cada item del checklist
            for (ChecklistItem item : items) {
                inspeccionDao.insertChecklistItem(item);
            }

            // Actualiza la inspección local CON el GPS y el nuevo estado
            inspeccionDao.updateInspectionGpsAndStatus(inspectionId, latitude, longitude, "pendiente_sync");

            Log.d(TAG, "Inspección " + inspectionId + " marcada para sincronizar con GPS: " + latitude);

            // Encola el WorkManager
            scheduleSync();

        });
    }

    // Encola el SyncWorker
    private void scheduleSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // Solo Wi-Fi
                .build();

        OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        workManager.enqueue(syncWorkRequest);
        Log.d(TAG, "SyncWorker encolado.");
    }
}