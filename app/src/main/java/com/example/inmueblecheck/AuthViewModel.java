package com.example.inmueblecheck;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AuthViewModel";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mDb;

    // LiveData para comunicar el estado a los Fragments
    private final MutableLiveData<AuthResultState> _authResult = new MutableLiveData<>();
    public LiveData<AuthResultState> getAuthResult() {
        return _authResult;
    }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
    }

    /**
     * Inicia sesión de un usuario existente.
     */
    public void login(String email, String password) {
        // Indicar que estamos cargando
        _authResult.setValue(AuthResultState.loading());

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Obtener el rol antes de navegar
                        fetchUserRole(user);
                    } else {
                        _authResult.setValue(AuthResultState.error(Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });
    }

    /**
     * Registra un nuevo usuario y guarda su rol en Firestore.
     */
    public void register(String email, String password, String role) {
        // Indicar que estamos cargando
        _authResult.setValue(AuthResultState.loading());

        if (password.length() < 6) {
            _authResult.setValue(AuthResultState.error("La contraseña debe tener al menos 6 caracteres."));
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // El usuario se creó en Auth. Ahora guardamos su rol en Firestore.
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserRoleToFirestore(firebaseUser, role);
                        }
                    } else {
                        _authResult.setValue(AuthResultState.error(Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });
    }

    /**
     * Guarda la información del rol en Firestore (Fase 1, Paso 3)
     */
    private void saveUserRoleToFirestore(FirebaseUser user, String role) {
        String uid = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", user.getEmail());
        userData.put("role", role); // "agente" o "gerente"

        mDb.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario registrado y rol guardado en Firestore.");
                    // Éxito en el registro, pero enviamos al login (no éxito de rol)
                    _authResult.setValue(AuthResultState.registrationSuccess());
                    mAuth.signOut(); // Desloguear para que el usuario inicie sesión manualmente
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar rol en Firestore", e);
                    _authResult.setValue(AuthResultState.error("Error al guardar datos de usuario: " + e.getMessage()));
                });
    }

    /**
     * Obtiene el rol del usuario desde Firestore (Fase 1, Paso 4)
     */
    private void fetchUserRole(FirebaseUser user) {
        String uid = user.getUid();
        mDb.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        _authResult.setValue(AuthResultState.success(user, role));
                    } else {
                        _authResult.setValue(AuthResultState.error("No se encontraron datos de usuario (rol)."));
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    _authResult.setValue(AuthResultState.error("Error al obtener rol: " + e.getMessage()));
                    mAuth.signOut();
                });
    }

    /**
     * Clase interna para manejar los estados de autenticación
     */
    public static class AuthResultState {
        public enum Status { SUCCESS, ERROR, LOADING, REGISTRATION_SUCCESS }

        public final Status status;
        public final FirebaseUser user;
        public final String role;
        public final String errorMessage;

        private AuthResultState(Status status, FirebaseUser user, String role, String errorMessage) {
            this.status = status;
            this.user = user;
            this.role = role;
            this.errorMessage = errorMessage;
        }

        public static AuthResultState loading() {
            return new AuthResultState(Status.LOADING, null, null, null);
        }

        public static AuthResultState success(FirebaseUser user, String role) {
            return new AuthResultState(Status.SUCCESS, user, role, null);
        }

        public static AuthResultState registrationSuccess() {
            return new AuthResultState(Status.REGISTRATION_SUCCESS, null, null, null);
        }

        public static AuthResultState error(String message) {
            return new AuthResultState(Status.ERROR, null, null, message);
        }
    }
}