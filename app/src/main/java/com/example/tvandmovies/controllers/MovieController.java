package com.example.tvandmovies.controllers;

import com.example.tvandmovies.api.MovieApi;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.model.ApiConfig;
import com.example.tvandmovies.model.Movie;
import com.example.tvandmovies.model.MovieResponse;
import com.example.tvandmovies.views.activities.MovieListActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieController {
    private final MovieListActivity view;
    private final MovieApi apiService;

    public MovieController(MovieListActivity view) {
        this.view = view;
        this.apiService = RetrofitClient.getClient().create(MovieApi.class);
    }


    // Filmek lekérése az API-tól
    public void loadMovies() {

        //Népszerű filmek hívása
        Call<MovieResponse> callPopularMovies = apiService.getPopularMovies(ApiConfig.API_KEY);
        callPopularMovies.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    view.updatePopularMovieList(movies);
                } else {
                    view.showError("Failed to load popular movies" + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                view.showError(t.getMessage());
            }
        });

        // Új filmek hívása
        Call<MovieResponse> callNewMovies = apiService.getNewPopularMovies(ApiConfig.API_KEY);
        callNewMovies.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    view.updateNewMovies(movies);
                } else {
                    view.showError("Failed to load upcoming movies" + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                view.showError(t.getMessage());
            }
        });


    }

}
