package com.example.tvandmovies.model.responses;

import com.google.gson.annotations.SerializedName;

public class TvDetailResponse {
    @SerializedName("number_of_seasons")
    private int numberOfSeasons;

    // Az utoljára leadott epizód a tévében/streamingben
    @SerializedName("last_episode_to_air")
    private EpisodeAirInfo lastEpisodeToAir;

    // A JÖVŐ: A következő epizód, ami érkezni fog (lehet null, ha vége a sorozatnak!)
    @SerializedName("next_episode_to_air")
    private EpisodeAirInfo nextEpisodeToAir;

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public EpisodeAirInfo getLastEpisodeToAir() {
        return lastEpisodeToAir;
    }

    public EpisodeAirInfo getNextEpisodeToAir() {
        return nextEpisodeToAir;
    }

    // --- BELSŐ OSZTÁLY AZ EPIZÓD INFÓKNAK ---
    public static class EpisodeAirInfo {
        @SerializedName("season_number")
        private int seasonNumber;

        @SerializedName("episode_number")
        private int episodeNumber;

        @SerializedName("air_date")
        private String airDate;

        public int getSeasonNumber() { return seasonNumber; }
        public int getEpisodeNumber() { return episodeNumber; }
        public String getAirDate() { return airDate; }
    }
}
