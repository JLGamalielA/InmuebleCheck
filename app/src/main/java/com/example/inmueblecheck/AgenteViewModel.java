package com.example.inmueblecheck;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AgenteViewModel extends ViewModel {

    private final MutableLiveData<List<Inspeccion>> inspecciones = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private ListenerRegistration listenerRegistration;

    public LiveData<List<Inspeccion>> getAllInspecciones() {
        if (listenerRegistration == null) {
            cargarInspeccionesEnTiempoReal();
        }
        return inspecciones;
    }

    public void recargarDatos() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        cargarInspeccionesEnTiempoReal();
    }

    private void cargarInspeccionesEnTiempoReal() {
        String uid = auth.getUid();
        if (uid == null) return;

        Query query = db.collection("inspecciones")
                .whereEqualTo("agentId", uid) // Asegúrate que en Firebase sea 'agentId'
                .orderBy("fechaCreacion", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("AgenteViewModel", "Error escuchando datos", e);
                return;
            }

            if (snapshots != null) {
                List<Inspeccion> lista = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Inspeccion inspeccion = doc.toObject(Inspeccion.class);
                    inspeccion.setDocumentId(doc.getId());
                    lista.add(inspeccion);
                }
                inspecciones.setValue(lista);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}