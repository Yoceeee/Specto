package com.example.tvandmovies.UI.home;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.repository.ContentRepository;
import com.example.tvandmovies.utilities.GenreHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeViewModel extends AndroidViewModel {
    private final ContentRepository repository;
    private final Handler slideshowHandler = new Handler(Looper.getMainLooper());
    private Runnable slideshowRunnable;
    private List<MediaItem> currentHeroItems = new ArrayList<>();
    private int currentHeroIndex = 0;
    private static final int SLIDESHOW_INTERVAL_MS = 4000;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> currentType = new MutableLiveData<>("movies");

    public final LiveData<List<MediaItem>> popularContent;
    public final LiveData<List<MediaItem>> newContent;
    public final LiveData<List<MediaItem>> allTimeBestContent;
    private final LiveData<List<MediaItem>> rawHeroList;
    private final MediatorLiveData<HeroUiState> heroState = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application application){
        super(application);
        this.repository = ContentRepository.getInstance(application);

        // A switchMap-eket a repository inicializálása UTÁN állítjuk be
        popularContent = Transformations.switchMap(currentType, type -> {
            isLoading.setValue(true);
            return "movies".equals(type) ? repository.getPopularMovies() : repository.getPopularSeries();
        });

        newContent = Transformations.switchMap(currentType, type -> {
            return "movies".equals(type) ? repository.getNewMovies() : repository.getNewSeries();
        });

        allTimeBestContent = Transformations.switchMap(currentType, type -> {
            if ("movies".equals(type)) return repository.getAllTimeBestMovies();
            return new MutableLiveData<>(Collections.emptyList());
        });

        rawHeroList = Transformations.switchMap(currentType, type -> {
            return "movies".equals(type) ? repository.getTrendingMovies() : repository.getTrendingSeries();
        });

        // Összekötjük a hero listát a slideshow logikával
        heroState.addSource(rawHeroList, this::updateHeroState);
    }

    // --- Getterek ---
    public LiveData<List<MediaItem>> getPopularContent() { return popularContent; }
    public LiveData<List<MediaItem>> getNewContent() { return newContent; }
    public LiveData<List<MediaItem>> getAllTimeBestContent() { return allTimeBestContent; }
    public LiveData<HeroUiState> getHeroState() { return heroState; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void setContentType(String type) {
        if (Objects.equals(type, currentType.getValue())) return;
        currentType.setValue(type);
    }

    // --- Hero Slideshow Logika ---
    private void updateHeroState(List<MediaItem> items) {
        stopPrevHero();
        
        if (items == null || items.isEmpty()) {
            currentHeroItems = new ArrayList<>();
            isLoading.setValue(false);
            return;
        }

        currentHeroItems = items.subList(0, Math.min(items.size(), 10));
        currentHeroIndex = 0;

        postNextHeroSlide();

        slideshowRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentHeroItems.isEmpty()) return;
                currentHeroIndex = (currentHeroIndex + 1) % currentHeroItems.size();
                postNextHeroSlide();
                slideshowHandler.postDelayed(this, SLIDESHOW_INTERVAL_MS);
            }
        };
        slideshowHandler.postDelayed(slideshowRunnable, SLIDESHOW_INTERVAL_MS);
        
        isLoading.setValue(false);
    }

    private void postNextHeroSlide() {
        if (currentHeroItems.isEmpty()) return;
        MediaItem item = currentHeroItems.get(currentHeroIndex);

        String imageUrl = item.getBackdropUrl();
        if (imageUrl == null || !imageUrl.startsWith("http")) {
            imageUrl = "https://image.tmdb.org/t/p/w780" + item.getPosterDetailUrl();
        }

        List<String> genreNames = new ArrayList<>();
        if (item.getGenreIds() != null) {
            for (int i = 0; i < Math.min(item.getGenreIds().size(), 3); i++) {
                String name = GenreHelper.getGenreName(item.getGenreIds().get(i));
                if (!name.isEmpty()) genreNames.add(name);
            }
        }

        heroState.setValue(new HeroUiState(item.getTitle(), String.join(" • ", genreNames), imageUrl, item));
    }

    private void stopPrevHero() {
        if (slideshowRunnable != null) slideshowHandler.removeCallbacks(slideshowRunnable);
    }

    // mentett állapot beállítása
    public void toggleSavedStatus(MediaItem item, boolean isCurrentlySaved){
        if (isCurrentlySaved) repository.deleteSaved(item);
        else repository.insertSavedContent(item);
    }

    public LiveData<List<MediaItem>> getAllSaved() { return repository.getAllSaved(); }
    public void refreshData() { repository.forceRefreshData(); }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopPrevHero();
    }

    public static class HeroUiState {
        public String title, genreText, imageUrl;
        public MediaItem originalItem;
        public HeroUiState(String title, String genreText, String imageUrl, MediaItem item) {
            this.title = title; this.genreText = genreText; this.imageUrl = imageUrl; this.originalItem = item;
        }
    }
}
