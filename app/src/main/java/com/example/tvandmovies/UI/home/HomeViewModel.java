package com.example.tvandmovies.UI.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final ContentRepository repository = ContentRepository.getInstance();

    // UI állapotok (MediatorLiveData-val)
    private final MediatorLiveData<List<MediaItem>> popularContent = new MediatorLiveData<>();
    private final MediatorLiveData<List<MediaItem>> newContent = new MediatorLiveData<>();
    private final MediatorLiveData<List<MediaItem>> allTimeBestContent = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> isLoading = new MediatorLiveData<>();
    private final MediatorLiveData<String> errorMessage = new MediatorLiveData<>();

    // segédváltozó a töltés figyeléséhez
    private boolean moviesLoaded = false;
    private boolean seriesLoaded = false;
    private String currentType = "movies";
    private boolean sourcesSet = false;

    // Forrás LiveData-k (a repository-ból)
    private LiveData<List<MediaItem>> popularMovies;
    private LiveData<List<MediaItem>> popularSeries;
    private LiveData<List<MediaItem>> newMovies;
    private LiveData<List<MediaItem>> newSeries;
    private LiveData<List<MediaItem>> allTimeBestMovies;

    public LiveData<List<MediaItem>> getPopularContent() {
        return popularContent;
    }
    public LiveData<List<MediaItem>> getNewContent() {
        return newContent;
    }
    public LiveData<List<MediaItem>> getAllTimeBestContent() {
        return allTimeBestContent;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<String> getErrorMessage() { return errorMessage; }


    // elemek betöltése/get-elése a nézetnek megfelelően
    public void setContentType(String type) {
        if (type.equals(currentType) && sourcesSet){
            return;
        }
        isLoading.setValue(true);
        removeCurrentSources();

        // beállítom a megfelelő kiválasztott típust
        currentType = type;

        if (type.equals("movies")){
            loadMoviesSources();
        } else if (type.equals("series")) {
            loadSeriesSources();
        }

        // Loading források hozzáadása/frissítése
        addLoadingSources();
        sourcesSet = true;
    }

    // Movies források betöltése (ha még nem)
    private void loadMoviesSources() {
        if (!moviesLoaded) {
            popularMovies = repository.getPopularMovies();
            newMovies = repository.getNewMovies();
            allTimeBestMovies = repository.getAllTimeBestMovies();
            moviesLoaded = true;
        }

        // Mediator-hoz hozzáadás
        popularContent.addSource(popularMovies, value -> {
            popularContent.setValue(value);
            checkLoading();
        });
        newContent.addSource(newMovies, value -> {
            newContent.setValue(value);
            checkLoading();
        });
        allTimeBestContent.addSource(allTimeBestMovies, value -> {
            allTimeBestContent.setValue(value);
            checkLoading();
        });
    }

    // Series források betöltése (ha még nem)
    private void loadSeriesSources() {
        if (!seriesLoaded) {
            popularSeries = repository.getPopularSeries();
            newSeries = repository.getNewSeries();
            seriesLoaded = true;
        }

        // Mediator-hoz hozzáadás
        popularContent.addSource(popularSeries, value -> {
            popularContent.setValue(value);
            checkLoading();
        });
        newContent.addSource(newSeries, value -> {
            newContent.setValue(value);
            checkLoading();
        });

        // AllTimeBest üres series esetén
        allTimeBestContent.setValue(new ArrayList<>());
    }

    // Régi források eltávolítása
    private void removeCurrentSources() {
        if (currentType.equals("movies")) {
            if (popularMovies != null) popularContent.removeSource(popularMovies);
            if (newMovies != null) newContent.removeSource(newMovies);
            if (allTimeBestMovies != null) allTimeBestContent.removeSource(allTimeBestMovies);
        } else if (currentType.equals("series")) {
            if (popularSeries != null) popularContent.removeSource(popularSeries);
            if (newSeries != null) newContent.removeSource(newSeries);
        }
    }

    // Loading source-ok hozzáadása (figyeli, ha value érkezik)
    private void addLoadingSources () {
        isLoading.removeSource(popularContent); // Eltávolítjuk a régieket, ha voltak
        isLoading.removeSource(newContent);
        isLoading.removeSource(allTimeBestContent);

        isLoading.addSource(popularContent, value -> checkLoading());
        isLoading.addSource(newContent, value -> checkLoading());
        isLoading.addSource(allTimeBestContent, value -> checkLoading());
    }

    // Loading ellenőrzés: ha minden source-nak van értéke, false-ra állítjuk
    private void checkLoading () {
        boolean loading = (popularContent.getValue() == null) ||
                (newContent.getValue() == null) ||
                (allTimeBestContent.getValue() == null);
        isLoading.setValue(loading);
    }
}
