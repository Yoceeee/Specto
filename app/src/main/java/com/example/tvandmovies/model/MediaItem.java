package com.example.tvandmovies.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

// Serializable interfész nem tartalmaz metódusokat, csak egy jelző interfész.
public class MediaItem implements Serializable {

    @SerializedName("id") // JSON kulcs, párosítja a java változót az api-ban szereplővel
    private int id; // 1197306

    @SerializedName(value = "release_date", alternate = {"first_air_date"})
    private Date reDate; // 2025-03-26

    @SerializedName("popularity")
    private double popularity; // 869.2977

    // ha filmes nézetben van az app, akkor a title lesz használva, ha a sorozatok, akkor a name-et fogja használni
    @SerializedName(value = "title", alternate = {"name"})
    private String title;

    @SerializedName("poster_path")
    private String posterUrl; // /xUkUZ8eOnrOnnJAfusZUqKYZiDu.jpg

    @SerializedName("overview")
    private String description; // Levon Cade left behind a decorated...

//    @SerializedName("genre_ids")
//    private int genreIds; // todo: itt több érték is jön, fel kell készíteni rá majd
    @SerializedName("vote_average")
    private double vote_avg; // 6.318

    @SerializedName("vote_count")
    private int vote_count; // 6.318

    // constructor
    public MediaItem(int id, Date reDate, double popularity, String title, String posterUrl, String description, double vote_avg, int vote_count) {
        this.id = id;
        this.reDate = reDate;
        this.popularity = popularity;
        this.title = title;
        this.posterUrl = posterUrl;
        this.description = description;
        this.vote_avg = vote_avg;
        this.vote_count = vote_count;
    }

    // a megfelelő kép betöltése érdekében
    public String getFullPosterUrl(){
        return "https://image.tmdb.org/t/p/w780" + posterUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getReDate() {
        return reDate;
    }

    public void setReDate(Date reDate) {
        this.reDate = reDate;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getVote_avg() {
        return vote_avg;
    }

    public void setVote_avg(double vote_avg) {
        this.vote_avg = vote_avg;
    }

    public double getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }
}
