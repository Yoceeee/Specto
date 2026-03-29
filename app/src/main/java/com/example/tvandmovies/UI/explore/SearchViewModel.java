package com.example.tvandmovies.UI.explore;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private final ContentRepository repository;

    // A nyers találatok (amit az API küldött, rendezés nélkül)
    private List<MediaItem> rawList = new ArrayList<>();

    // UI állapotok
    private final MediatorLiveData<List<MediaItem>> searchResults = new MediatorLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // a default kiválasztott kapcsoló az All lesz
    private FilterType currentSelectedFilter = FilterType.ALL;
    private String currentQuery = ""; // az éppen beírt szöveg

    // Segédváltozó az API hívások megfigyeléséhez
    private LiveData<List<MediaItem>> currentSource;
    private final Observer<List<MediaItem>> sourceObserver;

    public SearchViewModel(@NonNull Application application){
        super(application);
        repository = ContentRepository.getInstance(application);

        sourceObserver = items -> {
            if (items != null) {
                rawList = new ArrayList<>(items);
                updateUiList();
            } else {
                rawList.clear();
                searchResults.setValue(Collections.emptyList());
            }
            isLoading.setValue(false);
        };
    }

    // a filterek használatához szükséges kapcsolók
    public enum FilterType {
        ALL, NEW, POPULAR, MOVIES, SERIES
    }

    public LiveData<List<MediaItem>> getSearchResults() { return searchResults; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // Content mentésének szinkronizálásához
    public LiveData<List<MediaItem>> getAllSaved(){
        return repository.getAllSaved();
    }

    // keresés indítása, metódus, amit hív a chipButton kattintáskor
    public void setFilter(FilterType newFilter){
        // ha csak a rendezés változik, akkor nem kell újra API-val adatokat lehívni
        boolean needNewApiCall = isApiCallNeeded(this.currentSelectedFilter, newFilter);
        this.currentSelectedFilter = newFilter;

        if (currentQuery.length() < 3) return;

        // ha már van itt beírt szöveg, akkor új keresés indítása a megfelelő szűrő paraméterrel
        if (needNewApiCall){
            executeSearch();
        } else {
            updateUiList();
        }
    }

    // Segédfüggvény: kell-e új API hívás
    private boolean isApiCallNeeded(FilterType oldType, FilterType newType) {
        if (newType == FilterType.MOVIES || newType == FilterType.SERIES) return true;
        if (oldType == FilterType.MOVIES || oldType == FilterType.SERIES) return true;

        // Egyébként (ALL, NEW, POPULAR) ugyanaz a MultiSearch endpoint, csak a rendezés más.
        return false;
    }

    // mentési logika kapcsoló, törli (ha már ott van a listában, vagy menti, ha kell)
    public void toggleFavoriteStatus(MediaItem item, boolean isCurrentlySaved) {
        if (isCurrentlySaved) {
            repository.deleteSaved(item);
        } else {
            repository.insertSavedContent(item);
        }
    }

    // a beírt keresendő szöveg átadása a megfelelő API-nak -> indul a keresés
    public void performSearch(String query) {
        this.currentQuery = query;
        executeSearch();
    }

    // Rendezési / szűrési logika
    private void updateUiList() {
        if (rawList == null || rawList.isEmpty()){
            searchResults.setValue(Collections.emptyList());
            return;
        }

        // másolat lista rendezéshez
        List<MediaItem> sortedList = new ArrayList<>(rawList);

        if (currentSelectedFilter == FilterType.NEW) {
            // Dátum szerint csökkenő
            Collections.sort(sortedList, (o1, o2) -> {
                Date d1 = o1.getReDate();
                Date d2 = o2.getReDate();
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d2.compareTo(d1); // Fordított sorrend (legújabb elöl)
            });
        } else {
            Collections.sort(sortedList, (o1, o2) -> Double.compare(o2.getVote_avg(), o1.getVote_avg()));
        }
        searchResults.setValue(sortedList);
    }

    // keresés végrehajtása
    private void executeSearch() {
        isLoading.setValue(true);

        // korábbi forrás eltávolítása
        if (currentSource != null){
            searchResults.removeSource(currentSource);
        }

        switch (currentSelectedFilter){
            case MOVIES:
                currentSource = repository.searchMoviesOnly(currentQuery); break;
            case SERIES:
                currentSource = repository.searchTvAndSeriesOnly(currentQuery); break;
            default:
                currentSource = repository.searchMulti(currentQuery); break;
        }

        // Új forrás hozzácsatolása
        searchResults.addSource(currentSource, items -> {
            if (items != null) {
                rawList = new ArrayList<>(items);
                updateUiList(); // itt történik a rendezés + setValue
            } else {
                searchResults.setValue(Collections.emptyList());
            }
            isLoading.setValue(false);
        });
    }


    // Takarítás ha megszűnik a ViewModel, figyelés leállítása
    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentSource != null) {
            currentSource.removeObserver(sourceObserver);
        }
    }
}
