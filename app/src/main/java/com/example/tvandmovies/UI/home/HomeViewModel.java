package com.example.tvandmovies.UI.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;
import com.example.tvandmovies.utilities.GenreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeViewModel extends AndroidViewModel {
    private final ContentRepository repository;

    public HomeViewModel(@NonNull Application application){
        super(application);
        this.repository = ContentRepository.getInstance(application);
    }

    // UI állapotok (MediatorLiveData-val)
    private final MediatorLiveData<List<MediaItem>> popularContent = new MediatorLiveData<>();
    private final MediatorLiveData<List<MediaItem>> newContent = new MediatorLiveData<>();
    private final MediatorLiveData<List<MediaItem>> allTimeBestContent = new MediatorLiveData<>();

    private final MediatorLiveData<HeroUiState> heroState = new MediatorLiveData<>();
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
    private LiveData<List<MediaItem>> trendingMedia;

    public LiveData<List<MediaItem>> getPopularContent() {
        return popularContent;
    }
    public LiveData<List<MediaItem>> getNewContent() {
        return newContent;
    }
    public LiveData<List<MediaItem>> getAllTimeBestContent() {
        return allTimeBestContent;
    }
    public LiveData<HeroUiState> getHeroState() { return heroState; }
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
            trendingMedia = repository.getTrendingMovies();
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

        heroState.addSource(trendingMedia, items -> {
            if (items != null && !items.isEmpty()) {
                updateHeroState(items);
            }
        });
    }

    // Series források betöltése (ha még nem)
    private void loadSeriesSources() {
        if (!seriesLoaded) {
            popularSeries = repository.getPopularSeries();
            newSeries = repository.getNewSeries();
            trendingMedia = repository.getTrendingSeries();
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

        heroState.addSource(trendingMedia, items -> {
            if (items != null && !items.isEmpty()) {
                updateHeroState(items);
            }
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
            if (trendingMedia != null) heroState.removeSource(trendingMedia);
        } else if (currentType.equals("series")) {
            if (popularSeries != null) popularContent.removeSource(popularSeries);
            if (newSeries != null) newContent.removeSource(newSeries);
            if (trendingMedia != null) heroState.removeSource(trendingMedia);
        }
    }

    // Loading source-ok hozzáadása (figyeli, ha value érkezik)
    private void addLoadingSources () {
        isLoading.removeSource(popularContent); // Eltávolítjuk a régieket, ha voltak
        isLoading.removeSource(newContent);
        isLoading.removeSource(allTimeBestContent);
        isLoading.removeSource(heroState);

        isLoading.addSource(popularContent, value -> checkLoading());
        isLoading.addSource(newContent, value -> checkLoading());
        isLoading.addSource(allTimeBestContent, value -> checkLoading());
        isLoading.addSource(heroState, value -> checkLoading());
    }

    // Loading ellenőrzés: ha minden source-nak van értéke, false-ra állítjuk
    private void checkLoading () {
        boolean loading = (popularContent.getValue() == null) ||
                (newContent.getValue() == null) ||
                (allTimeBestContent.getValue() == null);
        isLoading.setValue(loading);
    }

    // livData-ba az összes mentett content-et
    public LiveData<List<MediaItem>> getAllSaved(){
        return repository.getAllSaved();
    }

    // mentés logika, ami töröl is, ha kell
    public void toggleSavedStatus(MediaItem item, boolean isCurrentlySaved){
        if (isCurrentlySaved){
            repository.deleteSaved(item);
        } else {
            repository.insertSavedContent(item);
        }
    }

    // Amikor megjön a lista a Repository-ból:
    private void updateHeroState(List<MediaItem> items) {
        if (items == null) return;

        int limit = Math.min(items.size(), 5);
        int randomIndex = new Random().nextInt(limit);
        MediaItem item = items.get(randomIndex);

        // Kép URL logika
        String imageUrl = item.getBackdropUrl();

        // Műfaj logika
        List<String> genreNames = new ArrayList<>();
        if (item.getGenreIds() != null) {
            int genreLimit = Math.min(item.getGenreIds().size(), 3);
            for (int i = 0; i < genreLimit; i++) {
                String name = GenreHelper.getGenreName(item.getGenreIds().get(i));
                if (!name.isEmpty()) genreNames.add(name);
            }
        }
        String formattedGenres;
        formattedGenres = String.join(" • ", genreNames);

        // Becsomagoljuk egy objektumba
        HeroUiState state = new HeroUiState(
                item.getTitle(),
                formattedGenres,
                imageUrl,
                item // Eltároljuk az eredeti objektumot is a kattintáshoz
        );

        heroState.setValue(state);
    }


    // hero header belső osztály az adatoknak
    public static class HeroUiState {
        public String title;
        public String genreText;
        public String imageUrl;
        public MediaItem originalItem;

        public HeroUiState(String title, String genreText, String imageUrl, MediaItem item) {
            this.title = title;
            this.genreText = genreText;
            this.imageUrl = imageUrl;
            this.originalItem = item;
        }
    }

}
