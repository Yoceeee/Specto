package com.example.tvandmovies.model.domain;

import com.example.tvandmovies.model.entities.WatchedEpisode;
import com.example.tvandmovies.model.responses.TvDetailResponse;

public class NextEpisodeInfo {
    public final WatchedEpisode lastWatched; // Helyi adat
    public final TvDetailResponse apiDetails; // Friss adat

    public NextEpisodeInfo(WatchedEpisode lastWatched, TvDetailResponse apiDetails) {
        this.lastWatched = lastWatched;
        this.apiDetails = apiDetails;
    }
}
