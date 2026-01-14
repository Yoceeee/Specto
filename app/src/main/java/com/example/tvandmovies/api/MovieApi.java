package com.example.tvandmovies.api;

import com.example.tvandmovies.model.ContentResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApi {
    // népszerű filmek
    @GET("movie/popular")
    Call<ContentResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    // új népszerű filmek
    @GET("movie/upcoming")
    Call<ContentResponse> getNewPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language
            );

    // adott film részleteinek lekérése
    @GET("movie/{movie_id}")
    Call<ContentResponse> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );

    // a keresőmező használata
    @GET("search/movie")
    Call<ContentResponse> searchMovie(
        @Query("query") String query,
        @Query("api_key") String apiKey,
        @Query("language") String language
    );


    // ------ SORZATOK ---------

    // a mai napon érkező sorozatok
    @GET("tv/airing_today")
    Call<ContentResponse> getAiringTodayTv(
        @Query("api_key") String apiKey,
        @Query("language") String language
    );

    // népszerű sorozatok
    @GET("tv/popular")
    Call<ContentResponse> getPopularSeries(
        @Query("api_key") String apiKey,
        @Query("language") String language
    );
}
