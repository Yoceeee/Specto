package com.example.tvandmovies.views.activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;
import com.example.tvandmovies.controllers.MovieController;
import com.example.tvandmovies.model.Movie;
import com.example.tvandmovies.views.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {
    private MovieController movieController;

    private RecyclerView popularMovieRecyclerView;
    private RecyclerView newMovieRecyclerView;

    private MovieAdapter popularMovieAdapter;
    private MovieAdapter newMovieAdapter;

    ProgressBar progressBarNew;
    ProgressBar progressBarPop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // tötltés jelző beállítása láthatóra
        progressBarPop = findViewById(R.id.popProgressBar);
        progressBarNew = findViewById(R.id.newProgressBar);

        progressBarPop.setVisibility(View.VISIBLE);
        progressBarNew.setVisibility(View.VISIBLE);

        // Népszerű filmek RecyclerView | vízszintes elrendezés beállítása
        popularMovieRecyclerView = findViewById(R.id.recyclerViewPopularMovie);

        popularMovieRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        // a túlpörgetés miatt bekövetkező hullámzást tiltja
        popularMovieRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        popularMovieAdapter = new MovieAdapter(new ArrayList<>());
        popularMovieRecyclerView.setAdapter(popularMovieAdapter);


        // új filmek RecyclerView
        newMovieRecyclerView = findViewById(R.id.recyclerViewNewMovie);
        newMovieRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        newMovieRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        newMovieAdapter = new MovieAdapter(new ArrayList<>());
        newMovieRecyclerView.setAdapter(newMovieAdapter);

        // Filmek betöltése
        movieController = new MovieController(this);
        movieController.loadMovies();

        // nem lesz sáv az app tetején
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    // Népszerű filmek beállítása az adapter segítségével
    public void updatePopularMovieList(List<Movie> movies) {
        popularMovieAdapter.setMovieList(movies);
        progressBarPop.setVisibility(View.GONE); // amint megjelennek a filmek eltünik a progbar
    }

    public void updateNewMovies(List<Movie> movies){
        newMovieAdapter.setMovieList(movies);
        progressBarNew.setVisibility(View.GONE);
    }
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}