package com.example.tvandmovies.UI.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.List;

public class SearchViewModel extends ViewModel {
    private final ContentRepository repository;

    // UI állapotok
    private final MutableLiveData<List<MediaItem>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public SearchViewModel(){
        repository = ContentRepository.getInstance();
    }

    public LiveData<List<MediaItem>> getSearchResults() { return searchResults; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // keresés indítása
    public void performSearch(String query) {
        isLoading.setValue(true);
        repository.searchContent(query, searchResults, errorMessage);

        // Megjegyzés: A Repository callbackje majd frissíti a searchResults-ot,
        // amit a Fragment figyel. Itt nem állítunk isLoading=false-t direktben,
        // azt majd a Fragmentben kezeljük, vagy a Repo-t kéne okosítani.
        // Egyszerűsítésként a Fragmentben levesszük a loadingot, ha jön adat.
    }
}
