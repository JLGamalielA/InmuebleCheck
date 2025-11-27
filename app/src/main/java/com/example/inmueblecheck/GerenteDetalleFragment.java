package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

public class GerenteDetalleFragment extends Fragment {

    private GerenteDetalleViewModel viewModel;
    private String inspectionId, direccion;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private NestedScrollView contentScrollView;
    private TextView tvDireccion, tvGps;
    private RecyclerView rvChecklist, rvMedia;
    private ChecklistReadOnlyAdapter checklistAdapter;
    private MediaAdapter mediaAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gerente_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener argumentos
        if (getArguments() != null) {
            inspectionId = getArguments().getString("inspectionId");
            direccion = getArguments().getString("direccion");
        }

        if (inspectionId == null) {
            Toast.makeText(getContext(), "Error: ID de inspección no válido.", Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        // Bindeo de Vistas
        toolbar = view.findViewById(R.id.toolbarGerenteDetalle);
        progressBar = view.findViewById(R.id.progressBarGerenteDetalle);
        contentScrollView = view.findViewById(R.id.contentScrollView);
        tvDireccion = view.findViewById(R.id.tvDireccionGerenteDetalle);
        tvGps = view.findViewById(R.id.tvGpsGerenteDetalle);
        rvChecklist = view.findViewById(R.id.recyclerViewChecklistGerente);
        rvMedia = view.findViewById(R.id.recyclerViewMediaGerente);
        String tituloToolbar = (direccion != null) ? direccion : "Detalle";
        toolbar.setTitle("Inspección: " + tituloToolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        setupRecyclerViews(); // Configurar RecyclerViews
        viewModel = new ViewModelProvider(this).get(GerenteDetalleViewModel.class); // Configurar ViewModel
        setupObservers(); // Configurar Observadores
        viewModel.loadInspeccionDetalle(inspectionId); // Cargar los datos
    }


    private void setupRecyclerViews() {
        // Checklist
        checklistAdapter = new ChecklistReadOnlyAdapter();
        rvChecklist.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChecklist.setAdapter(checklistAdapter);

        // Media (Galería)
        // Pasa el contexto para Glide
        mediaAdapter = new MediaAdapter(getContext());
        // Layout horizontal para la galería
        rvMedia.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMedia.setAdapter(mediaAdapter);
    }

    private void setupObservers() {
        // Observador de la Inspección (Dirección, GPS)
        viewModel.getInspeccion().observe(getViewLifecycleOwner(), inspeccion -> {
            if (inspeccion != null) {
                tvDireccion.setText(inspeccion.getDireccion());
                String gpsStatus = "GPS: " + inspeccion.getLatitud() + ", " + inspeccion.getLongitud()
                        + " (Estado: " + inspeccion.getStatus() + ")";
                tvGps.setText(gpsStatus);

                // Mostrar contenido cuando los datos principales cargan
                progressBar.setVisibility(View.GONE);
                contentScrollView.setVisibility(View.VISIBLE);
            }
        });

        // Observador del Checklist (Notas)
        viewModel.getChecklist().observe(getViewLifecycleOwner(), checklistMap -> {
            if (checklistMap != null) {
                checklistAdapter.setChecklistMap(checklistMap);
            }
        });

        // Observador de la Galería (Fotos/Videos)
        viewModel.getMediaList().observe(getViewLifecycleOwner(), mediaList -> {
            if (mediaList != null) {
                mediaAdapter.setMedia(mediaList);
            }
        });
    }
}