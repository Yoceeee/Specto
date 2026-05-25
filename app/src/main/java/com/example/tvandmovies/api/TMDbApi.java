package com.example.tvandmovies.api;

import com.example.tvandmovies.model.responses.ActorResponse;
import com.example.tvandmovies.model.responses.ContentResponse;
import com.example.tvandmovies.model.responses.CreditsResponse;
import com.example.tvandmovies.model.responses.OverviewAndTitleResponse;
import com.example.tvandmovies.model.responses.SeasonDetailResponse;
import com.example.tvandmovies.model.responses.TrailerResponse;
import com.example.tvandmovies.model.responses.TvDetailResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDbApi {

    // ---- Filmek ----
    // népszerű filmek
    @GET("movie/popular")
    Call<ContentResponse> getPopularMovies();

    // új filmek
    @GET("movie/upcoming")
    Call<ContentResponse> getNewPopularMovies();

    // minden idők legjobb filmjei
    @GET("movie/top_rated")
    Call<ContentResponse> getAllTimeTopMovies(
        @Query("page") int page
    );

    // kereséshez hívás
    @GET("search/movie")
    Call<ContentResponse> searchMovie(
            @Query("query") String query
    );


    // trending filmek a heroHeader-höz
    @GET("trending/{media_type}/{time_window}")
    Call<ContentResponse> getTrending(
            @Path("media_type") String mediaType, // "movie", "tv" vagy "all"
            @Path("time_window") String timeWindow // "day" vagy "week"
    );



    // univerzáláis kereséshez
    @GET("search/multi")
    Call<ContentResponse> searchMulti(
            @Query("query") String query
    );

    // népszerű személyek
    @GET("person/popular")
    Call<ActorResponse> getPopularActors();

    // ---- SZEREPLŐK ----
    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getMovieCredits(@Path("movie_id") int movieId);

    @GET("tv/{series_id}/credits")
    Call<CreditsResponse> getTvCredits(@Path("series_id") int seriesId);


    // ------ SORZATOK ---------

    // a mostanában futó sorozatok
    @GET("tv/airing_today")
    Call<ContentResponse> getAiringTodaySeries();

    // népszerű sorozatok
    @GET("tv/popular")
    Call<ContentResponse> getPopularSeries();

    // sorozatok kereséshez
    @GET("search/tv")
    Call<ContentResponse> searchTvAndSeries(
            @Query("query") String query
    );


    // ------- Sorozat epizódok -------
    @GET("tv/{series_id}/season/{season_number}")
    Call<SeasonDetailResponse> getSeasonDetails(
            @Path("series_id") int seriesId,
            @Path("season_number") int seasonNumber
    );

    // egy sorozat teljes évadainak számát adja vissza
    @GET("tv/{series_id}")
    Call<TvDetailResponse> getTvSeriesDetails(
            @Path("series_id") int seriesId
    );

    // ----- Segéd lekérdezések -----

    @GET("movie/{movie_id}")
    Call<OverviewAndTitleResponse> getMovieOverviewAndTitle(@Path("movie_id") int id);

    @GET("tv/{tv_id}")
    Call<OverviewAndTitleResponse> getTvOverviewAndTitle(@Path("tv_id") int id);

    // ------- Videók (Trailerek) lekérése -------
    @GET("{media_type}/{id}/videos")
    Call<TrailerResponse> getVideos(
            @Path("media_type") String mediaType, // "movie" vagy "tv"
            @Path("id") int id,
            @Query("language") String language
    );

    // ------- Ajánlott/Hasonló tartalmak -------
    @GET("{media_type}/{id}/recommendations")
    Call<ContentResponse> getRecommendations(
            @Path("media_type") String mediaType,
            @Path("id") int id
    );

    // ------- Sorozat ÉVAD szintű videók (Trailerek) -------
    @GET("tv/{series_id}/season/{season_number}/videos")
    Call<TrailerResponse> getSeasonVideos(
            @Path("series_id") int seriesId,
            @Path("season_number") int seasonNumber,
            @Query("language") String language
    );
}
