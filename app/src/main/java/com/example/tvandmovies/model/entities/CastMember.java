package com.example.tvandmovies.model.entities;

import com.google.gson.annotations.SerializedName;

public class CastMember {
    @SerializedName("name")
    private String name;

    @SerializedName("profile_path")
    private String profilePath;

    public String getName() { return name; }

    // Segédmetódus a kép URL-hez
    public String getProfileUrl() {
        if (profilePath != null && !profilePath.isEmpty()) {
            return "https://image.tmdb.org/t/p/w185" + profilePath;
        }
        return null;
    }
}
