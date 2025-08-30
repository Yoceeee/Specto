package com.example.tvandmovies.views.activities;


import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;
import com.example.tvandmovies.controllers.MovieController;
import com.example.tvandmovies.controllers.SearchController;
import com.example.tvandmovies.databinding.ActivitySearchMovieBinding;
import com.example.tvandmovies.model.Movie;
import com.example.tvandmovies.views.adapter.MovieAdapter;
import com.google.android.material.search.SearchBar;

import java.util.ArrayList;
import java.util.List;

public class SearchMovieActivity extends AppCompatActivity {
    private ActivitySearchMovieBinding binding;
    private SearchController searchController;
    private MovieAdapter movieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchMovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Teljes képernyős megjelenítés
        setupWindowFlags();
        movieAdapter = new MovieAdapter(new ArrayList<>(), true);
        setupRecyclerView(binding.recyclerView, movieAdapter);

        searchController = new SearchController(this);

        // keresőmező figyelése, aktív keresési mód
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                if (!query.trim().isEmpty()){
                    performSearch(query.trim());
                }
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    // teljes kijelzős mód
    private void setupWindowFlags() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    private void performSearch(String query) {
        searchController.searchMoviesAndSeries(query);
    }
    // a recyclerView beállítása
    private void setupRecyclerView(RecyclerView recyclerView, MovieAdapter adapter) {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(adapter);
    }

    public void updateSearchResults(List<Movie> movies) {
        movieAdapter.setMovieList(movies);
    }

    // toast message küldése hiba esetén
    public void showError(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}