package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextInputEditText etEmail, etPassword;
    private RadioGroup radioGroupRole;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Inicializar Vistas
        etEmail = view.findViewById(R.id.etEmailRegister);
        etPassword = view.findViewById(R.id.etPasswordRegister);
        radioGroupRole = view.findViewById(R.id.radioGroupRole);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin);
        progressBar = view.findViewById(R.id.progressBarRegister);

        setupClickListeners();
        setupObservers();
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
            String role = (selectedRoleId == R.id.radioGerente) ? "gerente" : "agente";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.register(email, password, role);
        });

        tvGoToLogin.setOnClickListener(v -> {
            // Navegar de vuelta a LoginFragment
            Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
        });
    }

    private void setupObservers() {
        authViewModel.getAuthResult().observe(getViewLifecycleOwner(), authResultState -> {
            switch (authResultState.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnRegister.setEnabled(false);
                    break;
                case REGISTRATION_SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(getContext(), "¡Registro exitoso! Por favor, inicia sesión.", Toast.LENGTH_LONG).show();
                    // Navegar de vuelta a Login
                    Navigation.findNavController(getView()).navigate(R.id.action_registerFragment_to_loginFragment);
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + authResultState.errorMessage, Toast.LENGTH_LONG).show();
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }
}