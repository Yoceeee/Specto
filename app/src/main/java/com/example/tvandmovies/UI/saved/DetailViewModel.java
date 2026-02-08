package com.example.tvandmovies.UI.saved;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

public class DetailViewModel extends AndroidViewModel {
    private final ContentRepository contentRepository;

    public DetailViewModel(@NonNull Application application){
        super(application);
        contentRepository = ContentRepository.getInstance(application);
    }

    // tétel mentése saját listába
    public void addToSaved(MediaItem item){
        contentRepository.insertSavedContent(item);
    }

    // tétel törlése a saját listából
    public void deleteFromSaved(MediaItem item){
        contentRepository.deleteSaved(item);
    }

    // adott content ellenőrzése
    public LiveData<MediaItem> getSavedById(int id){
        return contentRepository.getFavoriteById(id);
    }
}
