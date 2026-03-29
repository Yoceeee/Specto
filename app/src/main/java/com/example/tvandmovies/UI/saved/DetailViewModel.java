package com.example.tvandmovies.UI.saved;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tvandmovies.model.entities.WatchedEpisode;
import com.example.tvandmovies.model.responses.CreditsResponse;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.responses.SeasonDetailResponse;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.List;

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

    // szereplőgárda lekérdezése adott contenthez
    public LiveData<CreditsResponse> getCredits(int id, String mediaType) {
        return contentRepository.getCredits(id, mediaType);
    }

    // Egy adott sorozat adott évadjának lekérése
    public androidx.lifecycle.LiveData<SeasonDetailResponse> getSeasonDetails(int seriesId, int seasonNumber) {
        return contentRepository.getSeasonDetails(seriesId, seasonNumber);
    }

    // egy adott sorozat összes évadjának számát adja vissza
    public LiveData<Integer> getTvSeasonCount(int seriesId){
        return contentRepository.getTvSeasonCount(seriesId);
    }

    // ---- sorozatok epizódjainak kezelése ----
    public void insertWatchedEpisode(WatchedEpisode episode) {
        contentRepository.insertWatchedEpisode(episode);
    }

    public void deleteWatchedEpisode(WatchedEpisode episode) {
        contentRepository.deleteWatchedEpisode(episode);
    }

    public LiveData<List<WatchedEpisode>> getAllWatchedForSeries(int seriesId) {
        return contentRepository.getAllWatchedForSeries(seriesId);
    }
}
