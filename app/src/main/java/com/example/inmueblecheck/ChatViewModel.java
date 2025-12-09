package com.example.inmueblecheck;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;

    public LiveData<List<Message>> getMessages() { return messages; }

    public void loadMessages(String inmuebleId) {
        if (inmuebleId == null) return;

        listenerRegistration = db.collection("Inmuebles")
                .document(inmuebleId)
                .collection("chat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatViewModel", "Error cargando mensajes", error);
                        return;
                    }
                    if (value != null) {
                        List<Message> lista = value.toObjects(Message.class);
                        messages.setValue(lista);
                    }
                });
    }

    public void sendMessage(String inmuebleId, String text) {
        if (inmuebleId == null || text.trim().isEmpty()) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Message msg = new Message(text, uid, email);

        db.collection("Inmuebles")
                .document(inmuebleId)
                .collection("chat")
                .add(msg)
                .addOnFailureListener(e -> Log.e("ChatViewModel", "Error al enviar mensaje", e));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}