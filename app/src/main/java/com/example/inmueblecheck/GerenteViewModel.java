package com.example.inmueblecheck;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GerenteViewModel extends ViewModel {

    private static final String TAG = "GerenteViewModel";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<List<Inspeccion>> inspecciones = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    public GerenteViewModel() {
        fetchInspecciones();
    }
    public LiveData<List<Inspeccion>> getInspecciones() { return inspecciones; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSaveSuccess() { return saveSuccess; }
    public void resetSaveSuccess() { saveSuccess.setValue(false); }
    public void clearError() { error.setValue(null); }



    // Carga las inspecciones
    public void fetchInspecciones() {
        db.collection("inspecciones")
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen falló.", e);
                        error.setValue("Error al cargar inspecciones: " + e.getMessage());
                        return;
                    }

                    List<Inspeccion> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : value) {
                        Inspeccion insp = doc.toObject(Inspeccion.class);
                        if (insp != null) {
                            insp.setDocumentId(doc.getId());
                            lista.add(insp);
                        }
                    }
                    inspecciones.setValue(lista);
                });
    }

    // Carga la lista de agentes
    public LiveData<List<User>> fetchAgentes() {
        MutableLiveData<List<User>> agentes = new MutableLiveData<>();
        db.collection("users")
                .whereEqualTo("role", "agente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> listaAgentes = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User agente = doc.toObject(User.class);
                        if (agente != null) {
                            agente.setUid(doc.getId());
                            listaAgentes.add(agente);
                        }
                    }
                    agentes.setValue(listaAgentes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar agentes", e);
                    error.setValue("Error al cargar agentes");
                });
        return agentes;
    }

    // Crea una nueva inspección en Firestore
    public void crearInspeccion(String direccion, User agenteSeleccionado) {
        if (direccion.isEmpty() || agenteSeleccionado == null) {
            error.setValue("Dirección y Agente son requeridos");
            saveSuccess.setValue(false);
            return;
        }

        Inspeccion nuevaInspeccion = new Inspeccion(
                direccion,
                agenteSeleccionado.getUid(),
                agenteSeleccionado.getEmail()
        );

        db.collection("inspecciones")
                .add(nuevaInspeccion)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Inspección creada con ID: " + documentReference.getId());
                    saveSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear inspección", e);
                    error.setValue("Error al crear inspección");
                    saveSuccess.setValue(false);
                });
    }
}