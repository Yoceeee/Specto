package com.example.tvandmovies.model.entities;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public class Episode {
    private String formattedVoteAvg;

    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name; // Az epizód címe
    @SerializedName("overview")
    private String overview; // Rövid leírás
    @SerializedName("air_date")
    private String airDate; // Mikor jelent/jelenik meg?
    @SerializedName("episode_number")
    private int episodeNumber;
    @SerializedName("still_path")
    private String stillPath; // Az epizód kis képe (mint a poszter, csak fekvő),
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("runtime")
    private int runtime; // Hossz percekben

    @SerializedName("season_number")
    private int seasonNumber;


    // segéd az URL-hez, mint a MediaItem esetében
    public String getStillUrl() {
        if (stillPath != null && !stillPath.isEmpty()) {
            return "https://image.tmdb.org/t/p/w500" + stillPath;
        }
        return null;
    }

    // formázott értékelés
    public String getFormatedVoteAVG(){
        if (formattedVoteAvg == null){
            formattedVoteAvg = String.format(Locale.US, "%.1f", voteAverage);
        }
        return formattedVoteAvg;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getAirDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getStillPath() {
        return stillPath;
    }

    public void setStillPath(String stillPath) {
        this.stillPath = stillPath;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }
}
