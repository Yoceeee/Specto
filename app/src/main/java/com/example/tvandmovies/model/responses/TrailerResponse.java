package com.example.tvandmovies.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TrailerResponse {
    @SerializedName("results")
    private List<VideoResult> results;

    public List<VideoResult> getResults() {
        return results;
    }

    // Belső osztály az egyes videókhoz
    public static class VideoResult {
        @SerializedName("key")
        private String key; // a YouTube azonosító (pl. "dQw4w9WgXcQ")

        @SerializedName("site")
        private String site; // pl. "YouTube"

        @SerializedName("type")
        private String type; // pl. "Trailer", "Teaser"

        public String getKey() { return key; }
        public String getSite() { return site; }
        public String getType() { return type; }
    }
}
