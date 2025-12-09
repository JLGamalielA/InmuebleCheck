package com.example.inmueblecheck;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private String currentUserId;

    public ChatAdapter() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message, currentUserId);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessageText, tvTimestamp;
        LinearLayout messageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }

        public void bind(Message message, String currentUserId) {
            tvMessageText.setText(message.getText());
            tvSender.setText(message.getSenderEmail());

            if(message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                tvTimestamp.setText(sdf.format(message.getTimestamp()));
            }

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageContainer.getLayoutParams();

            if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
                // Mensaje enviado por mí (Derecha, Azul claro)
                params.gravity = Gravity.END;
                messageContainer.setBackgroundColor(Color.parseColor("#E3F2FD"));
            } else {
                // Mensaje recibido (Izquierda, Gris claro)
                params.gravity = Gravity.START;
                messageContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
            }
            messageContainer.setLayoutParams(params);
        }
    }
}