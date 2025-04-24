package com.example.tvandmovies;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.adapter.MovieAdapter;
import com.example.tvandmovies.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movieAdapter = new MovieAdapter(getDummyMovies());
        recyclerView.setAdapter(movieAdapter);
    }

    // ideiglenes töltelék adatok, később az API fogja szolgáltatni az adatokat
    // TODO API integráció
    private List<Movie> getDummyMovies(){
        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie("Inception", "https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_FMjpg_UX1000_.jpg"));
        movies.add(new Movie("Interstellar", "https://image.tmdb.org/t/p/w500/rAiYTfKGqDCRIIqo664sY9XZIvQ.jpg"));
        movies.add(new Movie("The Dark Knight", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"));
        movies.add(new Movie("The Avengers", "https://image.tmdb.org/t/p/w1280/obxwM7McFADgsGDLiaiKG2NK5PL.jpg"));
        movies.add(new Movie("Breaking Bad", "https://image.tmdb.org/t/p/w1280/rB2zuh010Qh7LS1qgqG319kTx0H.jpg"));
        movies.add(new Movie("Stranger Things", "https://image.tmdb.org/t/p/w1280/uOOtwVbSr4QDjAGIifLDwpb2Pdl.jpg"));

        return movies;
    }
}