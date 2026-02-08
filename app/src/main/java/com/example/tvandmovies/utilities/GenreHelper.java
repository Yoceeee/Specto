package com.example.tvandmovies.utilities;

import java.util.HashMap;
import java.util.Map;

public class GenreHelper {
    private static final Map<Integer, String> genreMap = new HashMap<>();
    static {
        // Film Műfajok (TMDB szabvány)
        genreMap.put(28, "Akció");
        genreMap.put(12, "Kaland");
        genreMap.put(16, "Animáció");
        genreMap.put(35, "Vígjáték");
        genreMap.put(80, "Bűnügyi");
        genreMap.put(99, "Dokumentum");
        genreMap.put(18, "Dráma");
        genreMap.put(10751, "Családi");
        genreMap.put(14, "Fantasy");
        genreMap.put(36, "Történelmi");
        genreMap.put(27, "Horror");
        genreMap.put(10402, "Zene");
        genreMap.put(9648, "Rejtély");
        genreMap.put(10749, "Romantikus");
        genreMap.put(878, "Sci-Fi");
        genreMap.put(10770, "TV Film");
        genreMap.put(53, "Thriller");
        genreMap.put(10752, "Háborús");
        genreMap.put(37, "Western");

        // Sorozat Műfajok (TV)
        genreMap.put(10759, "Akció & Kaland");
        genreMap.put(10762, "Gyerek");
        genreMap.put(10763, "Hírek");
        genreMap.put(10764, "Reality");
        genreMap.put(10765, "Sci-Fi & Fantasy");
        genreMap.put(10766, "Szappanopera");
        genreMap.put(10767, "Talk");
        genreMap.put(10768, "Háborús & Politika");
    }

    public static String getGenreName(int id) {
        String name = genreMap.get(id);
        return name != null ? name : "";
    }
}
