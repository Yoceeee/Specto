package com.example.tvandmovies.UI.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final ContentRepository repository;

    // UI állapotok (Live adataokkal)
    private final MutableLiveData<List<MediaItem>> popularContent = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newContent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // segédváltozó a töltés figyeléséhez
    private  int loadingCounter = 0;
    public HomeViewModel(){
        repository = ContentRepository.getInstance();
    }

    public LiveData<List<MediaItem>> getPopularContent() { return popularContent; }
    public LiveData<List<MediaItem>> getNewContent() { return newContent; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }


    // elemek betöltése/get-elése
    public void loadMovies(){
        startLoading(2);
        repository.getPopularMovies(popularContent, errorMessage, this::onRequestFinished);
        repository.getNewMovies(newContent, errorMessage, this::onRequestFinished);
    }

    public void loadSeries() {
        startLoading(2);
        repository.getPopularSeries(popularContent, errorMessage, this::onRequestFinished);
        repository.getNewSeries(newContent, errorMessage, this::onRequestFinished);
    }


    // töltés kezelése
    // TODO: (ez egy gyszerűsített megoldás, profibb lenne MediatorLiveData-val vagy Kotlin Coroutines-szal)
    private void startLoading(int requestCount){
        loadingCounter = requestCount;
        isLoading.setValue(true);
    }

    // amikor egy töltés befejeződött
    private void onRequestFinished(){
        loadingCounter--;
        if(loadingCounter <= 0){
            isLoading.setValue(false);
        }
    }
}
