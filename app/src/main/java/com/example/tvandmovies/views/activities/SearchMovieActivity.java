package com.example.tvandmovies.views.activities;


import android.os.Bundle;
import android.os.Handler;
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
import com.example.tvandmovies.utilities.FullScreenMode;
import com.example.tvandmovies.views.adapter.MovieAdapter;
import com.google.android.material.search.SearchBar;

import java.util.ArrayList;
import java.util.List;

public class SearchMovieActivity extends AppCompatActivity {
    private ActivitySearchMovieBinding binding;
    private SearchController searchController;
    private MovieAdapter movieAdapter;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchMovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // teljes kijelzős mód beállítása
        FullScreenMode.setupWindowFlags(this);

        movieAdapter = new MovieAdapter(new ArrayList<>(), true);
        setupRecyclerView(binding.recyclerView, movieAdapter);
        searchController = new SearchController(this);

        // keresőmező figyelése, aktív keresési mód
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            // csak akkor induljon a keresés, ha nem üres és nagyobb, mint 3 karaktert adott meg a user
            @Override
            public boolean onQueryTextChange(String newText) {

                if(searchRunnable != null){
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (newText.length() >= 3){
                    searchRunnable = () -> performSearch(newText);
                    searchHandler.postDelayed(searchRunnable, 500); // 500 ms-es várakozás
                }
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    //trigerreli a controllerben lévő API hívást minden query changenél
    private void performSearch(String query) {
        searchController.searchMoviesAndSeries(query);
    }

    // a recyclerView beállítása, horizontális nézetbe
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