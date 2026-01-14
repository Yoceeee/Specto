package com.example.tvandmovies.interfaces;

import com.example.tvandmovies.model.MediaItem;

import java.util.List;

public interface SearchViewInterface {

    // sikeres keresés után
    void updateSearchResults(List<MediaItem> mediaItems);

    // sikerteles keresés során
    void showError(String message);
}
