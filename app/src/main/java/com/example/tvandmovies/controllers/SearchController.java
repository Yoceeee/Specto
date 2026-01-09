package com.example.tvandmovies.controllers;

import com.example.tvandmovies.api.MovieApi;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.interfaces.SearchViewInterface;
import com.example.tvandmovies.model.ApiConfig;
import com.example.tvandmovies.model.MovieResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchController {
    private final SearchViewInterface view;
    private final MovieApi apiService;

    public SearchController(SearchViewInterface view) {
        this.view = view;
        this.apiService = RetrofitClient.getClient().create(MovieApi.class);
    }

    // filmek és sorozatok lekérése az API-val a kereséshez
    public void searchMoviesAndSeries(String query){
        Call<MovieResponse> search = apiService.searchMovie(query, ApiConfig.API_KEY, ApiConfig.LANGUAGE);

        // asszinkron keresés
        search.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    view.updateSearchResults(response.body().getResults());
                }else{
                    view.showError("Hiba történt keresés közben." +response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                view.showError(t.getMessage());
            }
        });
    }
}
