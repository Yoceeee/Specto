package com.example.tvandmovies.UI.explore;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.Collections;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private final ContentRepository repository;

    // UI állapotok
    private final MutableLiveData<List<MediaItem>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // a default kiválasztott kapcsoló az All lesz
    private FilterType currentSelectedFilter = FilterType.ALL;
    private String currentQuery = ""; // az éppen beírt szöveg

    public SearchViewModel(@NonNull Application application){
        super(application);
        repository = ContentRepository.getInstance(application);
    }

    // a filterek használatához szükséges kapcsolók
    public enum FilterType {
        ALL, NEW, POPULAR, MOVIES, SERIES
    }

    public LiveData<List<MediaItem>> getSearchResults() { return searchResults; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }


    // keresés indítása, metódus, amit hív a chipButton kattintáskor
    public void setFilter(FilterType newFilter){
        this.currentSelectedFilter = newFilter;

        // ha már van itt beírt szöveg, akkor új keresés indítása a megfelelő szűrő paraméterrel
        if (currentQuery != null &&  currentQuery.length() >= 3) {
            executeSearch(); // elindul újra a keresés
        }
    }

    // a beírt keresendő szöveg átadása a megfelelő API-nak
    public void performSearch(String query) {
        this.currentQuery = query;
        executeSearch();
    }

    // RENDEZÉS LOGIKA
    private List<MediaItem> applySorting(List<MediaItem> items) {
        if (currentSelectedFilter == FilterType.NEW) {
            // Dátum szerint csökkenő
            // TODO: Biztosítsd, hogy a MediaItem-ben legyen getDate() vagy kezeld le a null-t
            Collections.sort(items, (o1, o2) -> {
                String d1 = o1.getReDate().toString(); // Vagy getFirstAirDate()
                String d2 = o2.getReDate().toString();
                if (d1.isEmpty()) return 1;
                if (d2.isEmpty()) return -1;
                return d2.compareTo(d1); // Fordított sorrend (Legújabb elöl)
            });
        } else if (currentSelectedFilter == FilterType.ALL) {
            // Értékelés szerint csökkenő
            Collections.sort(items, (o1, o2) -> Double.compare(o2.getVote_avg(), o1.getVote_avg()));
        }
        return items;
    }

    // keresés megvalósítása
    private void executeSearch() {
        isLoading.setValue(true);
        LiveData<List<MediaItem>> source;

        switch (currentSelectedFilter){
            case MOVIES:
                source = repository.searchMoviesOnly(currentQuery); break;
            case SERIES:
                source = repository.searchTvAndSeriesOnly(currentQuery); break;
            case ALL:
                source = repository.searchMulti(currentQuery); break;
            case POPULAR:
                source = repository.searchMulti(currentQuery); break;
            case NEW:
                source = repository.searchMulti(currentQuery); break;
            default:
                source = repository.searchMulti(currentQuery); break;
        }

        source.observeForever(new Observer<List<MediaItem>>() {
            @Override
            public void onChanged(List<MediaItem> mediaItems) {
                if (mediaItems != null) {
                    searchResults.setValue(applySorting(mediaItems));
                }
                isLoading.setValue(false);

                // FONTOS: observer eltávolítása
                source.removeObserver(this);
            }
        });
    }
}
