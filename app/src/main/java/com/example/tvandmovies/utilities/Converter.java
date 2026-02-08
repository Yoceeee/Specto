package com.example.tvandmovies.utilities;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Converter {
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

    // mentéskor a műfajhoz átalakító: múfaj ([25,49] -> "[25,49]")
    @TypeConverter
    public static String fromIntegerList(List<Integer> genreIds) {
        if (genreIds == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {}.getType();
        return gson.toJson(genreIds, type);
    }

    // Betöltéskor: "[28,12]" -> [28, 12]
    @TypeConverter
    public static List<Integer> toIntegerList(String genreIdsString) {
        if (genreIdsString == null) {
            return Collections.emptyList();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(genreIdsString, type);
    }

}
