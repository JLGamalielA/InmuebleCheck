package com.example.inmueblecheck;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetalleInspeccionFragment extends Fragment implements ChecklistAdapter.ChecklistItemListener {

    private static final String TAG = "DetalleInspeccion";
    private DetalleInspeccionViewModel viewModel;
    private ChecklistAdapter adapter;
    private RecyclerView recyclerViewChecklist;
    private TextView tvDireccion, tvGpsStatus;
    private Button btnVerificarGps, btnFinalizarInspeccion;
    private ProgressBar progressBarDetalle;
    private MaterialToolbar toolbarDetalle;
    private FusedLocationProviderClient fusedLocationClient;
    private String inspectionId, direccion;
    private Location lastKnownLocation;
    private String currentPhotoPath;
    private Uri currentMediaUri;
    private String currentItemName; // Para saber a qué item pertenece la foto/video
    private String currentMediaType; // Para "photo" o "video"
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> videoLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        // Inicializar launchers
        registerPermissionLaunchers();
        registerMediaLaunchers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_inspeccion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDireccion = view.findViewById(R.id.tvDireccionDetalle);
        tvGpsStatus = view.findViewById(R.id.tvGpsStatus);
        btnVerificarGps = view.findViewById(R.id.btnVerificarGps);
        btnFinalizarInspeccion = view.findViewById(R.id.btnFinalizarInspeccion);
        progressBarDetalle = view.findViewById(R.id.progressBarDetalle);
        toolbarDetalle = view.findViewById(R.id.toolbarDetalle);
        recyclerViewChecklist = view.findViewById(R.id.recyclerViewChecklist);

        if (getArguments() != null) {
            inspectionId = getArguments().getString("inspectionId");
            direccion = getArguments().getString("direccion");
            tvDireccion.setText(direccion);
            toolbarDetalle.setTitle("Inspección: " + direccion);
        }
        viewModel = new ViewModelProvider(this).get(DetalleInspeccionViewModel.class);
        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        viewModel.loadChecklist(inspectionId);
    }

    private void setupRecyclerView() {
        adapter = new ChecklistAdapter(this);
        recyclerViewChecklist.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChecklist.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getChecklist().observe(getViewLifecycleOwner(), checklistItems -> {
            if (checklistItems != null) {
                adapter.setChecklist(checklistItems);
            }
        });

        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                Toast.makeText(getContext(), "Inspección guardada localmente y encolada para sincronizar.", Toast.LENGTH_LONG).show();
                // Regresar al dashboard del agente
                Navigation.findNavController(getView()).popBackStack();
            }
        });
    }

    private void setupClickListeners() {
        // Botón GPS
        btnVerificarGps.setOnClickListener(v -> checkLocationPermission());
        // Botón Finalizar
        btnFinalizarInspeccion.setOnClickListener(v -> {
            if (lastKnownLocation == null) {
                Toast.makeText(getContext(), "Debe verificar la ubicación GPS primero.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener la lista desde el adaptador
            List<ChecklistItem> items = adapter.getItems();

            // Guardar
            viewModel.saveOfflineEvidence(
                    inspectionId,
                    items,
                    lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude()
            );
        });

        // Botón de Volver (Toolbar)
        toolbarDetalle.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    @Override
    public void onCameraClick(String itemName) {
        this.currentItemName = itemName;
        checkCameraPermission("photo");
    }

    @Override
    public void onVideoClick(String itemName) {
        this.currentItemName = itemName;
        checkCameraPermission("video");
    }

    @Override
    public void onNotesChanged(String itemName, String notes) {

        if (adapter != null) {
            adapter.updateNotesForItem(itemName, notes);
        }
    }



    private void registerPermissionLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean cameraOk = result.get(Manifest.permission.CAMERA);

                    if (cameraOk != null && cameraOk) {
                        if ("photo".equals(currentMediaType)) {
                            dispatchTakePictureIntent();
                        } else {
                            dispatchTakeVideoIntent();
                        }
                    } else {
                        Toast.makeText(getContext(), "Permiso de Cámara denegado.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void getDeviceLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            lastKnownLocation = location;
                            String gpsText = "GPS OK: " + location.getLatitude() + ", " + location.getLongitude();
                            tvGpsStatus.setText(gpsText);
                            tvGpsStatus.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                            btnVerificarGps.setText("GPS Verificado");
                            btnVerificarGps.setEnabled(false);
                        } else {
                            tvGpsStatus.setText("No se pudo obtener ubicación. Intente de nuevo.");
                            Toast.makeText(getContext(), "Active el GPS y asegúrese de tener vista al cielo.", Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException en getDeviceLocation", e);
        }
    }

    private void checkCameraPermission(String type) {
        this.currentMediaType = type;
        cameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
    }

    private void registerMediaLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (currentMediaUri != null) {
                            Log.d(TAG, "Foto guardada en: " + currentMediaUri.toString());
                            viewModel.saveMedia(inspectionId, currentItemName, currentMediaUri.toString(), "image");
                        }
                    }
                });

        videoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (currentMediaUri != null) {
                            Log.d(TAG, "Video guardado en: " + currentMediaUri.toString());
                            viewModel.saveMedia(inspectionId, currentItemName, currentMediaUri.toString(), "video");
                        }
                    }
                });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + inspectionId + "_" + currentItemName + "_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath(); // Guardar path
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Error creando archivo de imagen", ex);
        }

        if (photoFile != null) {
            currentMediaUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.inmueblecheck.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentMediaUri);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String videoFileName = "MP4_" + inspectionId + "_" + currentItemName + "_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = File.createTempFile(
                videoFileName,
                ".mp4",
                storageDir
        );
        return video;
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File videoFile = null;
        try {
            videoFile = createVideoFile();
        } catch (IOException ex) {
            Log.e(TAG, "Error creando archivo de video", ex);
        }

        if (videoFile != null) {
            currentMediaUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.inmueblecheck.fileprovider",
                    videoFile);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentMediaUri);
            videoLauncher.launch(takeVideoIntent);
        }
    }
}