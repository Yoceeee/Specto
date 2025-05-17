package com.example.tvandmovies.views.activities;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.controllers.MovieController;
import com.example.tvandmovies.databinding.ActivityMainBinding;
import com.example.tvandmovies.model.Movie;
import com.example.tvandmovies.views.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {
    private MovieController movieController;
    private MovieAdapter popularMovieAdapter;
    private MovieAdapter newMovieAdapter;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // teljes képernyős megjelenítés
        setupWindowFlags();

        // a tötltés jelző beállítása láthatóra
        setLoading(true);

        // adapterek inicializálása
        popularMovieAdapter = new MovieAdapter(new ArrayList<>());
        newMovieAdapter = new MovieAdapter(new ArrayList<>());

        initRecyclerView(binding.recyclerViewPopularMovie, popularMovieAdapter);
        initRecyclerView(binding.recyclerViewNewMovie, newMovieAdapter);

        // Filmek betöltése
        movieController = new MovieController(this);
        movieController.loadMovies();

    }

    // a töltést jelző bar egyszerűbb használata
    private void setLoading(boolean isLoading) {
        binding.popProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.newProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    // A recyclerView-t itt inicializálom, mert több helyen kelleni fog
    private void initRecyclerView(RecyclerView recyclerView, MovieAdapter adapter){
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        // a túlpörgetés miatt bekövetkező hullámzást tiltja
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(adapter);
    }

    // nem fog megjelenni sáv az app tetején
    public void setupWindowFlags(){
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    // Népszerű filmek beállítása az adapter segítségével
    public void updatePopularMovieList(List<Movie> movies) {
        popularMovieAdapter.setMovieList(movies);
        setLoading(false); // amint megjelennek a filmek eltünik a progbar
    }

    public void updateNewMovies(List<Movie> movies){
        newMovieAdapter.setMovieList(movies);
        setLoading(false);
    }

    // toast message küldése hiba esetén
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}