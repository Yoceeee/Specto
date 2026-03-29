package com.example.tvandmovies.model.responses;

import com.google.gson.annotations.SerializedName;

public class TvDetailResponse {
    @SerializedName("number_of_seasons")
    private int numberOfSeasons;

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }
}
