package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class GerenteDashboardFragment extends Fragment {

    private GerenteViewModel viewModel;
    private RecyclerView recyclerView;
    private InspeccionAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private MaterialToolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gerente_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bindeo de Vistas
        recyclerView = view.findViewById(R.id.recyclerViewInspecciones);
        progressBar = view.findViewById(R.id.progressBar);
        fab = view.findViewById(R.id.fabCrearInspeccion);
        toolbar = view.findViewById(R.id.toolbarGerente);
        viewModel = new ViewModelProvider(requireActivity()).get(GerenteViewModel.class);
        progressBar.setVisibility(View.VISIBLE);

        setupRecyclerView();
        setupClickListeners();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new InspeccionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Listener de Clic (Bug #3)
        adapter.setOnInspeccionClickListener(inspeccion -> {
            if (getView() == null) return;
            if (inspeccion == null || inspeccion.getDocumentId() == null) {
                Toast.makeText(getContext(), "Error: Datos de inspección inválidos.", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle args = new Bundle();
            args.putString("inspectionId", inspeccion.getDocumentId());
            args.putString("direccion", inspeccion.getDireccion());

            Navigation.findNavController(getView()).navigate(
                    R.id.action_gerenteDashboardFragment_to_gerenteDetalleFragment,
                    args
            );
        });
    }

    private void setupClickListeners() {
        // Botón flotante
        fab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_gerenteDashboardFragment_to_crearInspeccionFragment);
        });

        // Menú (Cerrar Sesión)
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout_gerente) {
                FirebaseAuth.getInstance().signOut();
                Navigation.findNavController(getView()).navigate(R.id.loginFragment, null,
                        new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.nav_auth, true)
                                .build()
                );
                return true;
            }
            return false;
        });
    }

    private void setupObservers() {
        // Observador para la lista de inspecciones
        viewModel.getInspecciones().observe(getViewLifecycleOwner(), inspecciones -> {
            adapter.setInspecciones(inspecciones);
            progressBar.setVisibility(View.GONE);
            fab.show();
        });

        // Observador para errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }
}