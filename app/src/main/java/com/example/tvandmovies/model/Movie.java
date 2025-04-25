package com.example.tvandmovies.model;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("title")
    private String title;
    @SerializedName("poster_path")
    private String posterUrl;

    public Movie(String title, String posterUrl) {
        this.title = title;
        this.posterUrl = posterUrl;
    }

    // a megfelelő kép betöltése érdekében
    public String getFullPosterUrl(){
        return "https://image.tmdb.org/t/p/w500" + posterUrl;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}
