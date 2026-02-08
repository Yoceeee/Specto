package com.example.tvandmovies.UI.saved;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.List;

public class BookmarkViewModel extends AndroidViewModel {
    private final ContentRepository contentRepository;

    public BookmarkViewModel(@NonNull Application application){
        super(application);
        contentRepository = ContentRepository.getInstance(application);
    }

    // Ez adja vissza a listát a Room-ból (LiveData)
    public LiveData<List<MediaItem>> getFavorites() {
        return contentRepository.getAllSaved();
    }

    // törli a listából a conentet
    public void removeFromFavorites(MediaItem item) {
        contentRepository.deleteSaved(item);
    }
}
