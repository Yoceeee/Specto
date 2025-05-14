package com.example.tvandmovies.views.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.ItemMovieBinding;
import com.example.tvandmovies.model.Movie;
import com.example.tvandmovies.views.activities.MovieDetailActivity;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private final List<Movie> movies = new ArrayList<>(); // ez a lista tárolja a movie obj.-at

    // betölti a filmeket a movies listába
    public MovieAdapter(List<Movie> movies) {
        if (movies != null) {
            this.movies.addAll(movies);
        }
    }

    // TODO: majd elrakni egy utils mappába
    // a diffUtil osztálya
    private static class MovieDiffCallback extends DiffUtil.Callback{
        private final List<Movie> oldList;
        private final List<Movie> newList;

        public MovieDiffCallback(List<Movie> oldList, List<Movie> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size();}

        @Override
        public int getNewListSize() { return newList.size();}

        @Override
        public boolean areItemsTheSame(int oldItemPos, int newItemPos) {
            return oldList.get(oldItemPos).getId() == newList.get(newItemPos).getId();
        }
        @Override
        public boolean areContentsTheSame(int oldItemPos, int newItemPos) {
            return oldList.get(oldItemPos).equals(newList.get(newItemPos));
        }
    }

    // adatfrissítés, most már a DiffUtil használatával
    public void setMovieList(List<Movie> newMovies) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MovieDiffCallback(movies, newMovies));

        movies.clear();
        movies.addAll(newMovies);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMovieBinding binding = ItemMovieBinding.inflate(inflater, parent, false);
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieBinding binding;

        public MovieViewHolder(ItemMovieBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        // A használni kívánt filmes adatok beállítása bindelés használatával
        public void bind(Movie movie){
            binding.textTitle.setText(movie.getTitle() != null ? movie.getTitle() : "No title");
            binding.description.setText(movie.getDescription() != null ? movie.getDescription() : "No description");

            // Dátum formázása
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd", Locale.getDefault());
            try {
                if (movie.getReDate() != null) {
                    binding.releaseDate.setText(sdf.format(movie.getReDate()));
                } else {
                    binding.releaseDate.setText("N/A");
                }
            } catch (Exception e) {
                binding.releaseDate.setText("Helytelen dátum");
            }

            // Kép betöltése
            Glide.with(binding.getRoot().getContext())
                    .load(movie.getFullPosterUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                    .into(binding.imagePoster);

            // Kattintáskezelés, az detail nézet megnyitásához
            binding.getRoot().setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), MovieDetailActivity.class);
                intent.putExtra("object", movie);
                view.getContext().startActivity(intent);
            });
        }
    }
}