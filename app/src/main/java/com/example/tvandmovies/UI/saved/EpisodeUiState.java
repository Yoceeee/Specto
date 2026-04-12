package com.example.tvandmovies.UI.saved;

public class EpisodeUiState {
    public enum Status {
        NEXT_EPISODE, UPCOMING, CAUGHT_UP
    }

    private final Status status;
    private final int season;
    private final int episode;
    private final String date;


    // Konstruktor a következő részhez
    public EpisodeUiState(Status status, int season, int episode) {
        this.status = status;
        this.season = season;
        this.episode = episode;
        this.date = null;
    }

    // Konstruktor a jövőbeli részhez (dátummal)
    public EpisodeUiState(Status status, int season, int episode, String date) {
        this.status = status;
        this.season = season;
        this.episode = episode;
        this.date = date;
    }

    // Konstruktor a "naprakész" állapothoz
    public EpisodeUiState(Status status) {
        this.status = status;
        this.season = 0;
        this.episode = 0;
        this.date = null;
    }

    public Status getStatus() {
        return status;
    }

    public int getSeason() {
        return season;
    }

    public int getEpisode() {
        return episode;
    }

    public String getDate() {
        return date;
    }
}
