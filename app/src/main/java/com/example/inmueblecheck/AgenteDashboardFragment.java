package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class AgenteDashboardFragment extends Fragment {

    private AgenteViewModel viewModel;
    private RecyclerView recyclerView;
    private InspeccionAdapter adapter;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agente_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewInspeccionesAgente);
        progressBar = view.findViewById(R.id.progressBarAgente);
        toolbar = view.findViewById(R.id.toolbarAgente);

        viewModel = new ViewModelProvider(this).get(AgenteViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new InspeccionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnInspeccionClickListener(inspeccion -> {
            Bundle args = new Bundle();
            args.putString("inspectionId", inspeccion.getDocumentId());
            args.putString("direccion", inspeccion.getDireccion());

            Navigation.findNavController(getView()).navigate(
                    R.id.action_agenteDashboardFragment_to_detalleInspeccionFragment,
                    args
            );
        });
    }

    private void setupClickListeners() {
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout_agente) {
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

        viewModel.getAllInspecciones().observe(getViewLifecycleOwner(), inspecciones -> {
            if (inspecciones != null) {
                adapter.setInspecciones(inspecciones);
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
}