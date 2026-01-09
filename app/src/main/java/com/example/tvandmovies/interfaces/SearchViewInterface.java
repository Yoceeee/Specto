package com.example.tvandmovies.interfaces;

import com.example.tvandmovies.model.Movie;

import java.util.List;

public interface SearchViewInterface {

    // sikeres keresés után
    void updateSearchResults(List<Movie> movies);

    // sikerteles keresés során
    void showError(String message);
}
