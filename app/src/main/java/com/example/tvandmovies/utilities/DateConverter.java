package com.example.tvandmovies.utilities;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    // adatbázisból olvasáskor: szám (Long) -> Dátum (Date)
    @TypeConverter
    public static Date toDate(Long timestamp){
        return timestamp == null ? null : new Date(timestamp);
    }
    // Roomba íráskor: Date -> Long
    @TypeConverter
    public static Long toTimestamp(Date date){
        return date == null ? null : date.getTime();
    }
}
