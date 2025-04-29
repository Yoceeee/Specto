package com.example.tvandmovies.views.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
    private MovieAdapter movieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewMovie);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movieAdapter = new MovieAdapter(new ArrayList<>());
        recyclerView.setAdapter(movieAdapter);

        movieController = new MovieController(this);
        movieController.loadMovies();
    }

    public void updateMovieList(List<Movie> movies) {
        movieAdapter.setMovieList(movies);
    }

    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}