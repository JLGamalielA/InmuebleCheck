package com.example.inmueblecheck;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNetworkMonitor();
    }

    private void setupNetworkMonitor() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                runOnUiThread(() -> showOfflineMessage(true));
            }

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                runOnUiThread(() -> showOfflineMessage(false));
            }
        };

        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder().build(),
                networkCallback
        );
    }

    private void showOfflineMessage(boolean isOffline) {
        View rootView = findViewById(android.R.id.content);
        if (isOffline) {
            Snackbar.make(rootView, "Estás en Modo Offline. Los datos se guardarán localmente.", Snackbar.LENGTH_INDEFINITE)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                    .setTextColor(getResources().getColor(android.R.color.white))
                    .show();
        } else {
            Snackbar.make(rootView, "Conexión restaurada. Sincronizando...", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark))
                    .show();
            // Aquí podrías disparar el SyncWorker manualmente si lo deseas
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}