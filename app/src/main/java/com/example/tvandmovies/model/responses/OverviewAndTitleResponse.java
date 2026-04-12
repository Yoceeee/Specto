package com.example.tvandmovies.model.responses;

import com.google.gson.annotations.SerializedName;

public class OverviewAndTitleResponse {
    @SerializedName("overview")
    private String overview;
    @SerializedName("title")
    private String title;

    public String getOverview() {
        return overview;
    }

    public String getTitle(){
        return title;
    }
}
