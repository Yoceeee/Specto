package com.example.tvandmovies.model.domain;

import com.example.tvandmovies.model.entities.WatchedEpisode;
import com.example.tvandmovies.model.responses.TvDetailResponse;

public class NextEpisodeInfo {
    public final WatchedEpisode lastWatched;
    public final TvDetailResponse apiDetails;

    public NextEpisodeInfo(WatchedEpisode lastWatched, TvDetailResponse apiDetails) {
        this.lastWatched = lastWatched;
        this.apiDetails = apiDetails;
    }
}
