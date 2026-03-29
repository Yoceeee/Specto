package com.example.tvandmovies.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tvandmovies.model.entities.WatchedEpisode;

import java.util.List;

@Dao
public interface WatchedEpisodeDao {

    // beillesztés a megtekintett-ek közé
    @Insert(onConflict = OnConflictStrategy.REPLACE) // ha már van ilyen tétel, akkor update
    void insertWatchedEpisode(WatchedEpisode watchedEpisode);

    // törlés
    @Delete
    void deleteWatchedEpisode(WatchedEpisode watchedEpisode);

    // az adott epizód állapotának vizsgálata, hogy a user látta-e már
    @Query("SELECT * FROM watched_episode_table WHERE userId = :userId AND seriesId = :seriesId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber LIMIT 1")
    LiveData<WatchedEpisode> getWatchedEpisode(String userId, int seriesId, int seasonNumber,  int episodeNumber);

    // a folyamatban lévő táblában ezzel vizsgáljuk, hogy hol tart a folyamat
    @Query("SELECT * FROM watched_episode_table WHERE userId = :userId AND seriesId = :seriesId")
    LiveData<List<WatchedEpisode>> getAllWatchedForSeries(String userId, int seriesId);
}
