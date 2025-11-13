package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.textfield.TextInputEditText;
import com.example.inmueblecheck.R;

public class LoginFragment extends Fragment {
    private AuthViewModel authViewModel;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister);
        progressBar = view.findViewById(R.id.progressBar);
        setupClickListeners();
        setupObservers();
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.login(email, password);
        });

        tvGoToRegister.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }

    private void setupObservers() {
        authViewModel.getAuthResult().observe(getViewLifecycleOwner(), authResultState -> {
            switch (authResultState.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(false);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(getContext(), "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                    if ("gerente".equals(authResultState.role)) {
                        // Navega al dashboard de Gerente
                        Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_gerenteDashboardFragment);
                    } else {
                        // Navega al dashboard de Agente
                        Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_agenteDashboardFragment);
                    }
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + authResultState.errorMessage, Toast.LENGTH_LONG).show();
                    break;
                case REGISTRATION_SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }
}