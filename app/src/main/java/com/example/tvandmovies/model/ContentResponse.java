package com.example.tvandmovies.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContentResponse {
    @SerializedName("results")
    private List<MediaItem> results;

    public List<MediaItem> getResults() {
        return results;
    }
}
