package com.example.inmueblecheck;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@Entity(tableName = "inspecciones_table")
public class Inspeccion {

    @PrimaryKey
    @NonNull
    private String documentId;
    private String direccion;
    private String agentId;
    private String agentEmail;
    private String status;

    @ServerTimestamp
    private Date fechaCreacion;
    private double latitud;
    private double longitud;
    public Inspeccion() {
    }

    public Inspeccion(String direccion, String agentId, String agentEmail) {
        this.direccion = direccion;
        this.agentId = agentId;
        this.agentEmail = agentEmail;
        this.status = "pendiente";
    }

    // --- Getters y Setters ---
    @NonNull
    public String getDocumentId() { return documentId; }
    public void setDocumentId(@NonNull String documentId) { this.documentId = documentId; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getAgentEmail() { return agentEmail; }
    public void setAgentEmail(String agentEmail) { this.agentEmail = agentEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
}