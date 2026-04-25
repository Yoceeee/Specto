package com.example.tvandmovies.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tvandmovies.model.entities.SearchHistory;

import java.util.List;

@Dao
public interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistoryItem(SearchHistory item);

    // a 10 legutobbi keresesi elozmeny lekrdezese (felhasználó szinten)
    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT 10")
    LiveData<List<SearchHistory>> getRecentHistory(String userId);

    // adott keresesi elozmeny torlese
    @Query("DELETE FROM search_history WHERE id = :itemId AND userId = :userId")
    void deleteHistoryItem(int itemId, String userId);

    // az osszes keresesi elozmeny torlese
    @Query("DELETE FROM search_history WHERE userId = :userId")
    void clearAllHistory(String userId);
}
