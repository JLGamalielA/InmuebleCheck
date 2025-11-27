package com.example.inmueblecheck;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class AgenteDashboardFragment extends Fragment {

    private AgenteViewModel viewModel;
    private RecyclerView recyclerView;
    private InspeccionAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvPendingCount, tvCompletedCount;
    private ExtendedFloatingActionButton btnCrearInspeccion;
    private ImageView ivProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agente_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvInspecciones);
        progressBar = view.findViewById(R.id.progressBarAgente);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshAgente);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        btnCrearInspeccion = view.findViewById(R.id.btnCrearInspeccion);
        ivProfile = view.findViewById(R.id.ivProfile);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_primary);

        viewModel = new ViewModelProvider(this).get(AgenteViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        setupObservers();

        // Cargar datos
        viewModel.getAllInspecciones();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.getAllInspecciones();
        }
    }

    private void setupRecyclerView() {
        adapter = new InspeccionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnInspeccionClickListener(inspeccion -> {
            if (!isAdded()) {
                Log.w("AgenteDashboard", "Fragment no está adjunto al Activity");
                return;
            }

            try {
                // Validar objeto Inspección
                if (inspeccion == null) {
                    Toast.makeText(getContext(), "Error: Inspección nula", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validar ID del Documento
                String docId = inspeccion.getDocumentId();
                Log.d("AgenteDashboard", "Intentando abrir inspección con ID: " + docId);

                if (docId == null || docId.isEmpty()) {
                    Toast.makeText(getContext(), "Error: ID de inspección no válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Preparar Argumentos
                Bundle args = new Bundle();
                args.putString("inspectionId", docId);
                args.putString("direccion", inspeccion.getDireccion() != null ? inspeccion.getDireccion() : "Sin dirección");

                // Navegar con seguridad
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_agenteDashboardFragment_to_detalleInspeccionFragment, args);

            } catch (IllegalArgumentException e) {
                Log.e("AgenteDashboard", "Error de navegación: ID de acción no válido", e);
                Toast.makeText(getContext(), "Error al navegar. Verifica tu navigation.xml", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("AgenteDashboard", "Error al navegar", e);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        btnCrearInspeccion.setOnClickListener(v -> {
            if (!isAdded()) {
                return;
            }
            try {
                Navigation.findNavController(v).navigate(R.id.action_agenteDashboardFragment_to_crearInspeccionFragment);
            } catch (Exception e) {
                Log.e("AgenteDashboard", "Error navegando a crear", e);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        ivProfile.setOnClickListener(v -> mostrarDialogoLogout());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (viewModel != null) {
                viewModel.recargarDatos();
            }
        });
    }

    private void mostrarDialogoLogout() {
        new AlertDialog.Builder(getContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    try {
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.loginFragment, null,
                                new androidx.navigation.NavOptions.Builder()
                                        .setPopUpTo(R.id.nav_auth, true)
                                        .build()
                        );
                    } catch (Exception e) {
                        Log.e("AgenteDashboard", "Error logout", e);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupObservers() {
        viewModel.getAllInspecciones().observe(getViewLifecycleOwner(), inspecciones -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (inspecciones != null && !inspecciones.isEmpty()) {
                Log.d("AgenteDashboard", "Datos recibidos: " + inspecciones.size());
                adapter.setInspecciones(inspecciones);
                actualizarContadores(inspecciones);
            } else {
                Log.d("AgenteDashboard", "No hay datos disponibles");
                Toast.makeText(getContext(), "No hay inspecciones disponibles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarContadores(List<Inspeccion> lista) {
        int pendientes = 0;
        int completadas = 0;

        for (Inspeccion insp : lista) {
            String status = insp.getStatus();
            if (status != null && "completada".equalsIgnoreCase(status)) {
                completadas++;
            } else {
                pendientes++;
            }
        }

        tvPendingCount.setText(String.valueOf(pendientes));
        tvCompletedCount.setText(String.valueOf(completadas));
    }
}