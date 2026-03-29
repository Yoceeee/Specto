package com.example.tvandmovies.api;

import com.example.tvandmovies.model.responses.ContentResponse;
import com.example.tvandmovies.model.responses.CreditsResponse;
import com.example.tvandmovies.model.responses.SeasonDetailResponse;
import com.example.tvandmovies.model.responses.TvDetailResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApi {

    // ---- Filmek ----
    // népszerű filmek
    @GET("movie/popular")
    Call<ContentResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    // új filmek
    @GET("movie/upcoming")
    Call<ContentResponse> getNewPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    // minden idők legjobb filmjei
    @GET("movie/top_rated")
    Call<ContentResponse> getAllTimeTopMovies(
        @Query("api_key") String apiKey,
        @Query("language") String language,
        @Query("page") int page
    );

    // kereséshez hívás
    @GET("search/movie")
    Call<ContentResponse> searchMovie(
            @Query("query") String query,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );


    // adott film részleteinek lekérése
    @GET("movie/{movie_id}")
    Call<ContentResponse> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );

    // trending filmek a heroHeader-höz
    @GET("trending/{media_type}/{time_window}")
    Call<ContentResponse> getTrending(
            @Path("media_type") String mediaType, // "movie", "tv" vagy "all"
            @Path("time_window") String timeWindow, // "day" vagy "week"
            @Query("api_key") String apiKey,
            @Query("language") String language
    );



    // univerzáláis kereséshez
    @GET("search/multi")
    Call<ContentResponse> searchMulti(
            @Query("query") String query,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    // ---- SZEREPLŐK ----
    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getMovieCredits(@Path("movie_id") int movieId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("tv/{series_id}/credits")
    Call<CreditsResponse> getTvCredits(@Path("series_id") int seriesId, @Query("api_key") String apiKey, @Query("language") String language);


    // ------ SORZATOK ---------

    // a mostanában futó sorozatok
    @GET("tv/on_the_air")
    Call<ContentResponse> getOnTheAir(
        @Query("api_key") String apiKey,
        @Query("language") String language
    );

    // népszerű sorozatok
    @GET("tv/popular")
    Call<ContentResponse> getPopularSeries(
        @Query("api_key") String apiKey,
        @Query("language") String language
    );

    // sorozatok kereséshez
    @GET("search/tv")
    Call<ContentResponse> searchTvAndSeries(
            @Query("query") String query,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );


    // ------- Sorozat epizódok -------
    @GET("tv/{series_id}/season/{season_number}")
    Call<SeasonDetailResponse> getSeasonDetails(
            @Path("series_id") int seriesId,
            @Path("season_number") int seasonNumber,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    // egy sorozat teljes évadainak számát adja vissza
    @GET("tv/{series_id}")
    Call<TvDetailResponse> getTvSeriesDetails(
            @Path("series_id") int seriesId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
}
