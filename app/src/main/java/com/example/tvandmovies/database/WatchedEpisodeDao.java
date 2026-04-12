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

    // beillesztés a megtekintettek közé
    @Insert(onConflict = OnConflictStrategy.REPLACE) // ha már van ilyen tétel, akkor update
    void insertWatchedEpisode(WatchedEpisode watchedEpisode);

    // törlés megnézett részek közül
    @Delete
    void deleteWatchedEpisode(WatchedEpisode watchedEpisode);

    // az adott epizód állapotának vizsgálata, hogy a user látta-e már
    @Query("SELECT * FROM watched_episode_table WHERE userId = :userId AND seriesId = :seriesId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber LIMIT 1")
    LiveData<WatchedEpisode> getWatchedEpisode(String userId, int seriesId, int seasonNumber,  int episodeNumber);

    // a folyamatban lévő táblában ezzel vizsgáljuk, hogy hol tart a folyamat
    @Query("SELECT * FROM watched_episode_table WHERE userId = :userId AND seriesId = :seriesId")
    LiveData<List<WatchedEpisode>> getAllWatchedForSeries(String userId, int seriesId);


    // a reaktív verzió
    @Query("SELECT * FROM watched_episode_table WHERE userId = :userId AND seriesId = :seriesId ORDER BY seasonNumber DESC, episodeNumber DESC LIMIT 1")
    LiveData<WatchedEpisode> getLastWatchedEpisodeLive(String userId, int seriesId);

    // szinkron verzió (ezt várja a ViewModel háttérszála)
    @Query("SELECT * FROM watched_episode_table WHERE userId = :userId AND seriesId = :seriesId ORDER BY seasonNumber DESC, episodeNumber DESC LIMIT 1")
    WatchedEpisode getLastWatchedEpisodeSyncObj(String userId, int seriesId);


    // kijelentkezés során törlésre kerülnek a usernél tárolt adatok
    @Query("DELETE FROM watched_episode_table")
    void deleteAll();
}
