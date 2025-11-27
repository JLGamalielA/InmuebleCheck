package com.example.inmueblecheck;

import android.Manifest;
import android.app.Activity;
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
    private Uri currentMediaUri;
    private String currentItemName;
    private String currentMediaType;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> videoLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }

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

        try {
            // Inicializar vistas
            tvDireccion = view.findViewById(R.id.tvDireccionDetalle);
            tvGpsStatus = view.findViewById(R.id.tvGpsStatus);
            btnVerificarGps = view.findViewById(R.id.btnVerificarGps);
            btnFinalizarInspeccion = view.findViewById(R.id.btnFinalizarInspeccion);
            progressBarDetalle = view.findViewById(R.id.progressBarDetalle);
            toolbarDetalle = view.findViewById(R.id.toolbarDetalle);
            recyclerViewChecklist = view.findViewById(R.id.recyclerViewChecklist);

            if (progressBarDetalle != null) {
                progressBarDetalle.setVisibility(View.VISIBLE);
            }
            if (recyclerViewChecklist != null) {
                recyclerViewChecklist.setVisibility(View.GONE);
            }

            // Obtener argumentos
            if (getArguments() != null) {
                inspectionId = getArguments().getString("inspectionId");
                direccion = getArguments().getString("direccion");
                Log.d(TAG, "Argumentos recibidos - ID: " + inspectionId + ", Dirección: " + direccion);
            }

            // Validar inspectionId
            if (inspectionId == null || inspectionId.isEmpty()) {
                Log.e(TAG, "inspectionId es nulo o vacío");
                Toast.makeText(getContext(), "Error: ID de inspección no válido.", Toast.LENGTH_LONG).show();
                Navigation.findNavController(view).popBackStack();
                return;
            }

            // Mostrar información
            String titulo = (direccion != null && !direccion.isEmpty()) ? direccion : "Detalle";
            if (tvDireccion != null) {
                tvDireccion.setText(titulo);
            }
            if (toolbarDetalle != null) {
                toolbarDetalle.setTitle("Inspección: " + titulo);
                toolbarDetalle.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
            }

            // Inicializar ViewModel
            viewModel = new ViewModelProvider(this).get(DetalleInspeccionViewModel.class);
            // Configurar RecyclerView
            setupRecyclerView();
            // Configurar listeners
            setupClickListeners();
            // Configurar observers - AQUÍ ES DONDE ESTABA EL ERROR
            setupObservers();
            // Cargar datos
            viewModel.loadChecklist(inspectionId);

        } catch (Exception e) {
            Log.e(TAG, "Error en onViewCreated", e);
            Toast.makeText(getContext(), "Error al cargar la inspección", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        }
    }

    private void setupRecyclerView() {
        if (recyclerViewChecklist == null) {
            Log.e(TAG, "recyclerViewChecklist es nulo");
            return;
        }

        adapter = new ChecklistAdapter(this);
        recyclerViewChecklist.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChecklist.setAdapter(adapter);
        Log.d(TAG, "RecyclerView configurado");
    }

    private void setupObservers() {
        if (viewModel == null) {
            Log.e(TAG, "ViewModel es nulo en setupObservers");
            Toast.makeText(getContext(), "Error: ViewModel no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Observer para el checklist
            viewModel.getChecklist().observe(getViewLifecycleOwner(), checklistItems -> {
                Log.d(TAG, "Checklist actualizado: " + (checklistItems != null ? checklistItems.size() : 0) + " items");

                if (progressBarDetalle != null) {
                    progressBarDetalle.setVisibility(View.GONE);
                }

                if (checklistItems != null && !checklistItems.isEmpty()) {
                    if (adapter != null) {
                        adapter.setChecklist(checklistItems);
                    }
                    if (recyclerViewChecklist != null) {
                        recyclerViewChecklist.setVisibility(View.VISIBLE);
                    }
                    Log.d(TAG, "Checklist mostrado con " + checklistItems.size() + " items");
                } else {
                    Log.w(TAG, "Checklist vacío o nulo");
                    if (recyclerViewChecklist != null) {
                        recyclerViewChecklist.setVisibility(View.GONE);
                    }
                    if (tvDireccion != null) {
                        tvDireccion.setText("No hay items en el checklist");
                    }
                }
            });

            // estado de guardado
            viewModel.getSaveStatus().observe(getViewLifecycleOwner(), status -> {
                Log.d(TAG, "Status de guardado: " + status);

                if (status != null && status) {
                    Toast.makeText(getContext(), "Inspección guardada correctamente.", Toast.LENGTH_LONG).show();
                    if (getView() != null) {
                        Navigation.findNavController(getView()).popBackStack();
                    }
                } else if (status != null) {
                    Toast.makeText(getContext(), "Error al guardar la inspección.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error configurando observers", e);
            Toast.makeText(getContext(), "Error al configurar observadores", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        if (btnVerificarGps != null) {
            btnVerificarGps.setOnClickListener(v -> checkLocationPermission());
        }

        if (btnFinalizarInspeccion != null) {
            btnFinalizarInspeccion.setOnClickListener(v -> {
                if (lastKnownLocation == null) {
                    Toast.makeText(getContext(), "Debe verificar la ubicación GPS primero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (adapter != null) {
                    List<ChecklistItem> items = adapter.getItems();
                    if (items != null && !items.isEmpty()) {
                        viewModel.saveOfflineEvidence(
                                inspectionId,
                                items,
                                lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()
                        );
                    } else {
                        Toast.makeText(getContext(), "No hay items para guardar", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        getDeviceLocation();
                    } else {
                        Toast.makeText(getContext(), "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
                        if (tvGpsStatus != null) {
                            tvGpsStatus.setText("Se requiere permiso de ubicación.");
                        }
                    }
                });

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
        if (getContext() == null) {
            Log.w(TAG, "Context es nulo en checkLocationPermission");
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getDeviceLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            lastKnownLocation = location;
                            String gpsText = "Ubicación: " + location.getLatitude() + ", " + location.getLongitude() + "\n(Toca para ver en Mapa)";
                            if (tvGpsStatus != null) {
                                tvGpsStatus.setText(gpsText);
                                tvGpsStatus.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                                tvGpsStatus.setOnClickListener(v -> {
                                    Uri gmmIntentUri = Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude() + "?q=" + location.getLatitude() + "," + location.getLongitude() + "(" + direccion + ")");
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                                        startActivity(mapIntent);
                                    } else {
                                        Toast.makeText(getContext(), "Instala Google Maps para ver la ubicación", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            if (btnVerificarGps != null) {
                                btnVerificarGps.setText("GPS Verificado");
                                btnVerificarGps.setEnabled(false);
                            }
                        } else {
                            if (tvGpsStatus != null) {
                                tvGpsStatus.setText("No se pudo obtener ubicación. Intente de nuevo.");
                            }
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
                            if (viewModel != null) {
                                viewModel.saveMedia(inspectionId, currentItemName, currentMediaUri.toString(), "image");
                            }
                        }
                    }
                });

        videoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (currentMediaUri != null) {
                            Log.d(TAG, "Video guardado en: " + currentMediaUri.toString());
                            if (viewModel != null) {
                                viewModel.saveMedia(inspectionId, currentItemName, currentMediaUri.toString(), "video");
                            }
                        }
                    }
                });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + inspectionId + "_" + currentItemName + "_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
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
        return File.createTempFile(videoFileName, ".mp4", storageDir);
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