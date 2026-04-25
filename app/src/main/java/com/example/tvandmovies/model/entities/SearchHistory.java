package com.example.tvandmovies.model.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity(tableName = "search_history")
public class SearchHistory implements Serializable {
    @PrimaryKey
    private int id;
    private String userId;
    private String title;
    private String posterPath;
    private String mediaType;
    private double voteAverage;
    private Date releaseDate;
    private List<Integer> genreIds;
    private long timestamp; // a rendezéshez

    public SearchHistory() {
    }

    public SearchHistory(int id, String userId, String title, String posterPath, String mediaType, double voteAverage, Date releaseDate, List<Integer> genreIds, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.posterPath = posterPath;
        this.mediaType = mediaType;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.genreIds = genreIds;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Segédfüggvény: SearchHistory átalakítása MediaItem-mé,
    public MediaItem toMediaItem() {
        MediaItem item = new MediaItem();
        item.setId(this.id);
        item.setTitle(this.title);
        item.setPosterUrl(this.posterPath);
        item.setMediaType(this.mediaType);
        item.setVote_avg(this.voteAverage);
        item.setReDate(this.releaseDate);
        item.setGenreIds(this.genreIds);
        return item;
    }
}
