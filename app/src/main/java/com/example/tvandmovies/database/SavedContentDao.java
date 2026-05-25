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
    @Query("SELECT * FROM savedContent_table WHERE user_id = :userID ORDER BY title ASC")
    LiveData<List<MediaItem>> getAllSavedContent(String userID);

    // Ellenőrzés (Egy konkrét film / sorozat)
    // Ezzel nézzük meg, hogy egy adott content (id alapján) mentve van-e.
    // Ha null-t ad vissza, nincs mentve. Ha objektumot, akkor mentve van.
    @Query("SELECT * FROM savedContent_table WHERE id = :id and user_id = :userID LIMIT 1")
    LiveData<MediaItem> getSavedContentById(int id, String userID);

    // Ezt használja a Repository háttérszála a szinkron ellenőrzéshez, a folyamatban lévő sorozatok esetén (Nincs LiveData)
    @Query("SELECT * FROM savedContent_table WHERE id = :id and user_id = :userID LIMIT 1")
    MediaItem getSavedContentByIdSync(int id, String userID);

    // FOLYAMATBAN LÉVŐ SOROZATOK (Continue Watching)
    // Összekapcsoljuk a mentett tartalmakat a megnézett epizódokkal, userId szerint
    @Query("SELECT DISTINCT s.* FROM savedContent_table s " +
            "INNER JOIN watched_episode_table w ON s.id = w.seriesId " +
            "WHERE s.user_id = :userID AND w.userId = :userID AND s.is_watched = 0")
    LiveData<List<MediaItem>> getContinueWatchingSeries(String userID);

    // a workManager-nek szükséges szinkron lekérdezés, csak azokat nézzük ahol az értesítés be van kapcsolva
    @Query("SELECT DISTINCT s.* FROM savedContent_table s " +
            "INNER JOIN watched_episode_table w ON s.id = w.seriesId " +
            "WHERE s.user_id = :userID AND w.userId = :userID AND s.is_notification_enabled = 1 AND s.is_watched = 0")
    List<MediaItem> getContinueWatchingSeriesSync(String userID);


    // a már megtekintett filmek számának lekérdezése
    @Query("SELECT COUNT(*) FROM savedContent_table WHERE user_id = :userId AND (media_type = 'movie' OR media_type = 'movies') AND is_watched = 1")
    LiveData<Integer> getWatchedMoviesCount(String userId);

    @Query("SELECT COUNT(*) FROM savedContent_table WHERE user_id = :userId AND (media_type = 'tv' OR media_type = 'series') AND is_watched = 1")
    LiveData<Integer> getWatchedSeriesCount(String userId);

    // azok a contentek, ahol be van állítva értesítés
    @Query("SELECT * FROM savedContent_table WHERE user_id = :userID AND is_notification_enabled = 1 ORDER BY title ASC")
    LiveData<List<MediaItem>> getNotifiedSeries(String userID);
}
