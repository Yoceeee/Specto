package com.example.tvandmovies.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

// Serializable interfész nem tartalmaz metódusokat, csak egy jelző interfész.
public class MediaItem implements Serializable {
    private String formattedRating; // az imdb pontszámhoz

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

    // formázott imdb pontszám
    public String getFormatedRating(){
        if (formattedRating == null){
            formattedRating = String.format(Locale.US, "%.1f", vote_avg);
        }
        return formattedRating;
    }

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

    // a DiffUtil-hoz szükséges, hogy a valódi változást tudja trackelni
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaItem that = (MediaItem) o;

        return id == that.id &&
                Double.compare(that.popularity, popularity) == 0 &&
                Double.compare(that.vote_avg, vote_avg) == 0 &&
                vote_count == that.vote_count &&
                Objects.equals(reDate, that.reDate) &&
                Objects.equals(title, that.title) &&
                Objects.equals(posterUrl, that.posterUrl) &&
                Objects.equals(description, that.description);
    }

    public int hashCode(){
        return Objects.hash(
                id,
                reDate,
                popularity,
                title,
                posterUrl,
                description,
                vote_avg,
                vote_count
        );
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
    public double getPopularity() {
        return popularity;
    }
    public String getTitle() {
        return title;
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
    public double getVote_count() {
        return vote_count;
    }
}
