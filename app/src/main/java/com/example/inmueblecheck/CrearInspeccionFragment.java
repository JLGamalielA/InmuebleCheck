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

import java.util.ArrayList;
import java.util.List;

public class CrearInspeccionFragment extends BottomSheetDialogFragment {

    private GerenteViewModel viewModel;
    private TextInputEditText etDireccion;
    private AutoCompleteTextView spinnerAgente;
    private Button btnCrear;
    private List<User> listaDeAgentes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_crear_inspeccion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDireccion = view.findViewById(R.id.etDireccion);
        spinnerAgente = view.findViewById(R.id.spinnerAgente);
        btnCrear = view.findViewById(R.id.btnCrear);
        viewModel = new ViewModelProvider(requireActivity()).get(GerenteViewModel.class);
        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        viewModel.fetchAgentes().observe(getViewLifecycleOwner(), agentes -> {
            if (agentes != null) {
                this.listaDeAgentes = agentes;
                List<String> emails = new ArrayList<>();
                for (User agente : agentes) {
                    if (agente.getEmail() != null) {
                        emails.add(agente.getEmail());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, emails);
                spinnerAgente.setAdapter(adapter);
            }
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Inspección creada exitosamente", Toast.LENGTH_SHORT).show();
                viewModel.resetSaveSuccess();
                dismiss();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }

    private void setupClickListeners() {
        btnCrear.setOnClickListener(v -> {
            // Validar Dirección
            String direccion = "";
            if (etDireccion.getText() != null) {
                direccion = etDireccion.getText().toString().trim();
            }
            if (direccion.isEmpty()) {
                etDireccion.setError("La dirección es requerida");
                return;
            }

            // Validar Selección de Agente
            String emailSeleccionado = spinnerAgente.getText().toString();
            if (emailSeleccionado.isEmpty()) {
                spinnerAgente.setError("Debes seleccionar un agente");
                return;
            }

            // Buscar usuario correspondiente al email seleccionado
            User agenteSeleccionado = null;
            if (listaDeAgentes != null) {
                for (User agente : listaDeAgentes) {
                    if (agente.getEmail() != null && agente.getEmail().equals(emailSeleccionado)) {
                        agenteSeleccionado = agente;
                        break;
                    }
                }
            }

            if (agenteSeleccionado != null) {
                viewModel.crearInspeccion(direccion, agenteSeleccionado);
            } else {
                spinnerAgente.setError("El agente seleccionado no es válido");
                Toast.makeText(getContext(), "El agente seleccionado no es válido", Toast.LENGTH_SHORT).show();
            }
        });
    }
}