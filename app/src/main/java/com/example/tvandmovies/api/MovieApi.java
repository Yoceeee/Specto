package com.example.tvandmovies.api;

import com.example.tvandmovies.model.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApi {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/upcoming")
    Call<MovieResponse> getNewPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/{movie_id}")
    Call<MovieResponse> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );


}
