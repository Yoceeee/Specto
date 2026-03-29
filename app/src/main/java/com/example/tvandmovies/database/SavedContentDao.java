package com.example.tvandmovies.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tvandmovies.model.entities.MediaItem;

import java.util.List;

@Dao
public interface SavedContentDao {
    // Mentés
    // ha már létezik adott ID-val film -> csere
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaItem mediaItem);

    // Törlés
    @Delete
    void delete(MediaItem mediaItem);

    // Lekérdezés (mentett tartalom / user)
    // TODO: mentési dátum szerint legyen a query
    @Query("SELECT * FROM savedContent_table WHERE user_ID = :userID ORDER BY title ASC")
    LiveData<List<MediaItem>> getAllSavedContent(String userID);

    // Ellenőrzés (Egy konkrét film / sorozat)
    // Ezzel nézzük meg, hogy egy adott content (id alapján) mentve van-e.
    // Ha null-t ad vissza, nincs mentve. Ha objektumot, akkor mentve van.
    @Query("SELECT * FROM savedContent_table WHERE id = :id and user_id = :userID LIMIT 1")
    LiveData<MediaItem> getSavedContentById(int id, String userID);
}
