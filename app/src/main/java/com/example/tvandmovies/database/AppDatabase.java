package com.example.tvandmovies.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.utilities.DateConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// konfiguráció megadása
@Database(entities = {MediaItem.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class}) // dátum konvertáló használata
public abstract class AppDatabase extends RoomDatabase {
    public abstract SavedContentDao savedContentDao();

    // singleton séma (csak 1x fog futni)
    private static volatile AppDatabase INSTANCE;

    // háttérszál kezelés (executor): fő szálon és UI-on nem szabad írni adatbázisba -> kap egy fix 4 szálas poolt
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =  Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // példányosítás
    public static AppDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "savedContent_table")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
