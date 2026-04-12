package com.example.tvandmovies.model.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "watched_episode_table",
        primaryKeys = {"userId", "seriesId", "seasonNumber", "episodeNumber"})
public class WatchedEpisode {
    @NonNull
    private String userId = "";
    private int seriesId;
    private int seasonNumber;
    private int episodeNumber;
    private long watchedAtTimestamp; // megnézés időpontja

    // üres konstruktor a Roomnak és a Firebasenek
    public WatchedEpisode(){}

    public WatchedEpisode(@NonNull String userId, int seriesId, int seasonNumber, int episodeNumber, long watchedAtTimestamp) {
        this.userId = userId;
        this.seriesId = seriesId;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.watchedAtTimestamp = watchedAtTimestamp;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public int getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(int seriesId) {
        this.seriesId = seriesId;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public long getWatchedAtTimestamp() {
        return watchedAtTimestamp;
    }

    public void setWatchedAtTimestamp(long watchedAtTimestamp) {
        this.watchedAtTimestamp = watchedAtTimestamp;
    }
}
