package com.example.tvandmovies.UI.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
    private final List<String> genres;
    private final OnGenreClickListener listener;

    public interface OnGenreClickListener {
        void onGenreClick(String genreName);
    }

    // Konstruktor listener nélkül (pl. ActivityContentDetail-hez)
    public GenreAdapter(List<String> genres) {
        this(genres, null);
    }

    // Konstruktor listenerrel (pl. HomeFragment-hez)
    public GenreAdapter(List<String> genres, OnGenreClickListener listener) {
        this.genres = genres;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_chip, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        String genre = genres.get(position);
        holder.genreName.setText(genre);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGenreClick(genre);
            }
        });
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    static class GenreViewHolder extends RecyclerView.ViewHolder {
        TextView genreName;
        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            genreName = itemView.findViewById(R.id.genreName);
        }
    }
}
