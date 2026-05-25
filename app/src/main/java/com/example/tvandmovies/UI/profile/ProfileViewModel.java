package com.example.tvandmovies.UI.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {
    private ContentRepository repository;
    private LiveData<Integer> watchedMoviesCount;
    private LiveData<Integer> watchedSeriesCount;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = ContentRepository.getInstance(application);
        watchedMoviesCount = repository.getWatchedMoviesCount();
        watchedSeriesCount = repository.getWatchedSeriesCount();
    }

    public LiveData<Integer> getWatchedMoviesCount() {
        return watchedMoviesCount;
    }

    public LiveData<Integer> getWatchedSeriesCount() {
        return watchedSeriesCount;
    }

    public LiveData<List<MediaItem>> getAllSaved() {
        return repository.getAllSaved();
    }

    public LiveData<List<MediaItem>> getNotifiedSeries() {
        return repository.getNotifiedSeries();
    }
}
