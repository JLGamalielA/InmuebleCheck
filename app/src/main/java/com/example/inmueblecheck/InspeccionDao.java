package com.example.inmueblecheck;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface InspeccionDao {

    // --- MÉTODOS DE INSPECCION ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Inspeccion> inspecciones);

    @Query("SELECT * FROM inspecciones_table ORDER BY fechaCreacion DESC")
    LiveData<List<Inspeccion>> getAllInspecciones();

    @Query("DELETE FROM inspecciones_table")
    void deleteAll();

    // Actualiza solo el estado (usado por el SyncWorker)
    @Query("UPDATE inspecciones_table SET status = :status WHERE documentId = :inspectionId")
    void updateInspectionStatus(String inspectionId, String status);

    // Actualiza estado, latitud y longitud
    @Query("UPDATE inspecciones_table SET status = :status, latitud = :lat, longitud = :lon WHERE documentId = :inspectionId")
    void updateInspectionGpsAndStatus(String inspectionId, double lat, double lon, String status);

    // --- MÉTODOS DE CHECKLIST ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChecklistItem(ChecklistItem item);

    @Query("SELECT * FROM checklist_table WHERE inspectionId = :inspectionId")
    LiveData<List<ChecklistItem>> getChecklistForInspection(String inspectionId);

    // --- MÉTODOS DE MEDIA ---
    @Insert
    void insertMedia(Media media);

    @Query("SELECT * FROM media_table WHERE inspectionId = :inspectionId")
    LiveData<List<Media>> getMediaForInspection(String inspectionId);

    // --- MÉTODOS DEL SYNCWORKER ---
    @Query("SELECT * FROM inspecciones_table WHERE status = 'pendiente_sync'")
    List<Inspeccion> getPendingInspectionsToSync();

    @Query("SELECT * FROM checklist_table WHERE inspectionId = :inspectionId")
    List<ChecklistItem> getChecklistItemsToSync(String inspectionId);

    @Query("SELECT * FROM media_table WHERE inspectionId = :inspectionId AND isSynced = 0")
    List<Media> getMediaToSync(String inspectionId);

    @Update
    void updateMedia(Media media);
}