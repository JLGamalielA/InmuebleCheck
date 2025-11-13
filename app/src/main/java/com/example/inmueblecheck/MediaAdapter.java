package com.example.inmueblecheck;

import android.content.Context;
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
            String label = media.getItemName() + " (" + media.getType() + ")";
            tvMediaItemName.setText(label);

            if ("video".equals(media.getType())) {
                ivMedia.setImageResource(android.R.drawable.ic_media_play); // Placeholder para video
            } else {
                // Carga la imagen desde la URL de Firebase Storage
                Glide.with(context)
                        .load(media.getRemoteUri()) // Carga la URL remota
                        .placeholder(android.R.drawable.stat_sys_download) // Icono de "cargando"
                        .error(android.R.drawable.ic_dialog_alert) // Icono de error
                        .into(ivMedia);
            }
        }
    }
}