package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class CrearInspeccionFragment extends BottomSheetDialogFragment {

    private GerenteViewModel viewModel;
    private TextInputEditText etDireccion;
    private AutoCompleteTextView spinnerAgente;
    private Button btnCrear;
    private List<User> listaDeAgentes; // Para guardar la lista de agentes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_crear_inspeccion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bindeo de vistas
        etDireccion = view.findViewById(R.id.etDireccion);
        spinnerAgente = view.findViewById(R.id.spinnerAgente);
        btnCrear = view.findViewById(R.id.btnCrearInspeccion);

        // ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(GerenteViewModel.class);
        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        viewModel.fetchAgentes().observe(getViewLifecycleOwner(), agentes -> {
            if (agentes != null) {
                this.listaDeAgentes = agentes; // Guarda la lista
                // Extrae solo los emails para el ArrayAdapter
                List<String> emails = new java.util.ArrayList<>();
                for (User agente : agentes) {
                    emails.add(agente.getEmail());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, emails);
                spinnerAgente.setAdapter(adapter);
            }
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Inspección creada", Toast.LENGTH_SHORT).show();
                viewModel.resetSaveSuccess(); // Resetea el flag
                dismiss(); // Cierra el diálogo
            }
        });

        // Observador de Errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnCrear.setOnClickListener(v -> {
            String direccion = etDireccion.getText().toString().trim();
            String emailSeleccionado = spinnerAgente.getText().toString();

            User agenteSeleccionado = null;
            // Busca el objeto User completo basado en el email
            if (listaDeAgentes != null) {
                for (User agente : listaDeAgentes) {
                    if (agente.getEmail().equals(emailSeleccionado)) {
                        agenteSeleccionado = agente;
                        break;
                    }
                }
            }

            viewModel.crearInspeccion(direccion, agenteSeleccionado);
        });
    }
}