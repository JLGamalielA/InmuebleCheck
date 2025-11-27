package com.example.inmueblecheck;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<Media> mediaList = new ArrayList<>();
    private Context context;

    public MediaAdapter(Context context) {
        this.context = context;
    }

    public void setMedia(List<Media> mediaList) {
        this.mediaList = mediaList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = mediaList.get(position);
        holder.bind(media, context);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMedia;
        private TextView tvMediaItemName;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvMediaItemName = itemView.findViewById(R.id.tvMediaItemName);
        }

        public void bind(Media media, Context context) {
            String label = media.getItemName();
            tvMediaItemName.setText(label);

            // Configuración de Glide para imágenes y videos
            if ("video".equals(media.getType())) {
                // Glide puede generar miniaturas de videos locales o remotos
                Glide.with(context)
                        .asBitmap() // Forzar carga como imagen (frame del video)
                        .load(media.getRemoteUri() != null ? media.getRemoteUri() : media.getLocalUri())
                        .placeholder(android.R.drawable.ic_media_play)
                        .centerCrop()
                        .into(ivMedia);

                // Icono superpuesto para indicar que es video 
            } else {
                Glide.with(context)
                        .load(media.getRemoteUri() != null ? media.getRemoteUri() : media.getLocalUri())
                        .placeholder(android.R.drawable.stat_sys_download)
                        .error(android.R.drawable.ic_dialog_alert)
                        .centerCrop()
                        .into(ivMedia);
            }


            itemView.setOnClickListener(v -> {
                String uriString = media.getRemoteUri() != null ? media.getRemoteUri() : media.getLocalUri();
                if (uriString != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String mimeType = "video".equals(media.getType()) ? "video/*" : "image/*";
                    intent.setDataAndType(Uri.parse(uriString), mimeType);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        // Fallback si no hay app para abrir
                    }
                }
            });
            // ----------------------------------------------------------
        }
    }
}