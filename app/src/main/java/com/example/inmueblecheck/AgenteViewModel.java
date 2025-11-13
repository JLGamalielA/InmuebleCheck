package com.example.inmueblecheck;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class AgenteViewModel extends AndroidViewModel {

    private InspeccionRepository repository;
    private LiveData<List<Inspeccion>> allInspecciones;

    public AgenteViewModel(@NonNull Application application) {
        super(application);
        repository = new InspeccionRepository(application);

        allInspecciones = repository.getAllInspecciones();
    }

    public LiveData<List<Inspeccion>> getAllInspecciones() {
        return allInspecciones;
    }

}