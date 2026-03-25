package com.example.tvandmovies.UI.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tvandmovies.R;
import com.example.tvandmovies.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class SavedContentAdapter extends RecyclerView.Adapter<SavedContentAdapter.SavedViewHolder> {
    private List<MediaItem> savedItems = new ArrayList<>();
    private final OnItemClickListener listener;

    // Interface a kártyára kattintáshoz (részletek megnyitása)
    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
        void onItemLongClick(MediaItem item);
    }

    public SavedContentAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSavedItems(List<MediaItem> savedItems) {
        this.savedItems = savedItems;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_content, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        MediaItem item = savedItems.get(position);

        //Poszter betöltése Glide-dal
        Glide.with(holder.itemView.getContext())
                .load(item.getPosterThumbUrl())
                .centerCrop()
                .into(holder.imagePoster);

        holder.imdbScore.setText(item.getFormatedRating());
        holder.voteCount.setText(item.getFormatedVoteCount());

        // Hosszú kattintás kezelése
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(item);
            }
            return true;
        });

        // rövid kattintás kezelése
        holder.itemView.setOnClickListener(v ->{
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedItems != null ? savedItems.size() : 0;
    }

    // --- VIEW HOLDER ---
    static class SavedViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePoster;
        TextView imdbScore;
        TextView voteCount;

        public SavedViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePoster = itemView.findViewById(R.id.imagePoster);
            imdbScore = itemView.findViewById(R.id.imdbScore);
            voteCount = itemView.findViewById(R.id.voteCount);
        }
    }
}
