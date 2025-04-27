package com.example.tvandmovies.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.bumptech.glide.Glide;
import com.example.tvandmovies.model.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private final List<Movie> movies;

    public MovieAdapter(List<Movie> movies){
        this.movies = movies;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMovieList(List<Movie> movies) {
        this.movies.clear();
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // film címe
        holder.textTitle.setText(movie.getTitle());
        // film leírása
        holder.description.setText(movie.getDescription());

        //a dátum megjelenítéséhez át kell alakítani a date típust, hogy a setText() tudja kezelni
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM", Locale.getDefault());
        holder.releaseDate.setText(sdf.format(movie.getReDate()));

        //poster kép betöltése
        Glide.with(holder.itemView.getContext())
                .load(movie.getFullPosterUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(16))) // 16 px lekerekítés
                .into(holder.imagePoster);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView description;
        TextView releaseDate;
        ImageView imagePoster;

        public MovieViewHolder(@NonNull View itemView){
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            releaseDate = itemView.findViewById(R.id.release_date);
            description = itemView.findViewById(R.id.description);
            imagePoster = itemView.findViewById(R.id.imagePoster);
        }
    }
}
