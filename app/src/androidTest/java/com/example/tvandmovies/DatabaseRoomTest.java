package com.example.tvandmovies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.tvandmovies.database.AppDatabase;
import com.example.tvandmovies.database.SavedContentDao;
import com.example.tvandmovies.database.SearchHistoryDao;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.entities.SearchHistory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DatabaseRoomTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private SavedContentDao savedDao;
    private SearchHistoryDao historyDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Az inMemoryDatabase minden teszt futás után megsemmisül, így tiszta lappal indulunk
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        savedDao = db.savedContentDao();
        historyDao = db.searchHistoryDao();
    }

    @After
    public void closeDb() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void writeAndReadSavedContent() {
        MediaItem item = new MediaItem();
        item.setId(123);
        item.setUserId("user1");
        item.setTitle("Test Movie");
        item.setMediaType("movie");

        savedDao.insert(item);
        
        MediaItem retrieved = savedDao.getSavedContentByIdSync(123, "user1");
        assertNotNull("A mentett tartalom nem található az adatbázisban!", retrieved);
        assertEquals("Test Movie", retrieved.getTitle());
    }

    @Test
    public void deleteSavedContent() {
        MediaItem item = new MediaItem();
        item.setId(123);
        item.setUserId("user1");
        savedDao.insert(item);
        
        savedDao.delete(item);
        MediaItem retrieved = savedDao.getSavedContentByIdSync(123, "user1");
        assertNull("A törölt tartalom még mindig az adatbázisban van!", retrieved);
    }

    @Test
    public void historyLimitAndOrder() throws InterruptedException {
        String testUser = "user1";
        long baseTime = System.currentTimeMillis();
        
        // 15 elem beszúrása
        for (int i = 1; i <= 15; i++) {
            SearchHistory history = new SearchHistory(
                    i,
                    testUser,
                    "Query " + i,
                    "path/" + i,
                    "movie",
                    8.0,
                    null,
                    null,
                    baseTime + i
            );
            historyDao.insertHistoryItem(history);
        }

        // LiveData megvárása (max 2 mp)
        List<SearchHistory> recent = LiveDataTestUtil.getOrAwaitValue(historyDao.getRecentHistory(testUser));
        
        // 1. Csak a legutóbbi 10 elem maradhat meg (DAO LIMIT 10)
        assertEquals("A limit nem érvényesült!", 10, recent.size());
        
        // 2. A legfrissebbnek kell elöl lennie (ID: 15, mert i=15 volt az utolsó)
        assertEquals("Hibás sorrend: nem a legfrissebb az első!", 15, recent.get(0).getId());
        
        // 3. A 10. elemnek az i=6-nak kell lennie (15, 14, 13, 12, 11, 10, 9, 8, 7, 6)
        assertEquals("A 10. elem nem a várt (i=6) azonosítójú!", 6, recent.get(9).getId());
    }
}
