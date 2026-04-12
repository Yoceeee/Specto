package com.example.tvandmovies.UI.saved;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;


import com.example.tvandmovies.model.domain.NextEpisodeInfo;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.entities.WatchedEpisode;
import com.example.tvandmovies.model.responses.TvDetailResponse;
import com.example.tvandmovies.repository.ContentRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkViewModel extends AndroidViewModel {
    private final ContentRepository contentRepository;

    private final LiveData<List<MediaItem>> rawCountinueWatching;
    private final MediatorLiveData<List<ContinueWatchingDisplayModel>> combinedData = new MediatorLiveData<>();
    private final Map<Integer, LiveData<NextEpisodeInfo>> episodeDataMap = new HashMap<>();

    public BookmarkViewModel(@NonNull Application application){
        super(application);
        contentRepository = ContentRepository.getInstance(application);

        // a folyamatban lévő lista lekérdezése
        rawCountinueWatching = contentRepository.getContinueWatchingSeries();

        // mediator beállítása, hogy figyelje a kapott listát
        combinedData.addSource(rawCountinueWatching, seriesList ->{
            if (seriesList != null){
                processSeriesList(seriesList);
            }
        });
    }

    // Ez adja vissza a listát a Room-ból (LiveData)
    public LiveData<List<MediaItem>> getFavorites() {
        return contentRepository.getAllSaved();
    }

    // törli a listából a conentet
    public void removeFromFavorites(MediaItem item) {
        contentRepository.deleteSaved(item);
    }

    // Folyamatban lévő sorozatok (Continue Watching)
    public LiveData<List<ContinueWatchingDisplayModel>> getContinueWatching() { return combinedData; }

    // a folyamatban lévő sorozat állapotát lekérdezi
    private void processSeriesList(List<MediaItem> seriesList) {
        for (MediaItem item : seriesList) {
            // Ha még nem figyeltük ezt a sorozatot, elkezdjük
            if (!episodeDataMap.containsKey(item.getId())) {
                LiveData<NextEpisodeInfo> episodeInfo = contentRepository.getNextEpisodeRawData(item.getId());
                episodeDataMap.put(item.getId(), episodeInfo);

                // a Mediátor ezt az új forrást is figyelni fogja
                combinedData.addSource(episodeInfo, info -> updateCombinedList());
            }
        }
        updateCombinedList();
    }

    // a kombinált lista updatelése
    private void updateCombinedList() {
        List<MediaItem> series = rawCountinueWatching.getValue();
        if (series == null) return;

        List<ContinueWatchingDisplayModel> displayModels = new ArrayList<>();

        for (MediaItem item : series) {
            NextEpisodeInfo info = null;
            if (episodeDataMap.containsKey(item.getId())) {
                info = episodeDataMap.get(item.getId()).getValue();
            }

            // a szöveg formázása
            String status = formatStatusText(info);
            displayModels.add(new ContinueWatchingDisplayModel(item, status));
        }

        combinedData.setValue(displayModels);
    }

    // Kényszerített frissítés a cache ürítésével
    public void forceRefreshEpisodes() {
        // Leiratkozunk a régi adatokról
        for (LiveData<NextEpisodeInfo> oldSource : episodeDataMap.values()) {
            combinedData.removeSource(oldSource);
        }

        // Kiürítjük a gyorsítótárat
        episodeDataMap.clear();

        // Újraindítjuk a letöltést
        if (rawCountinueWatching.getValue() != null) {
            processSeriesList(rawCountinueWatching.getValue());
        }
    }

    // a folyamatban lévő sorozatok kártyáin megjelenő státusz szöveg formázása
    private String formatStatusText(NextEpisodeInfo info) {
        if (info == null) return "Betöltés...";
        if (info.apiDetails == null) return "Offline / Hiba";

        WatchedEpisode lastWatched = info.lastWatched;
        TvDetailResponse apiData = info.apiDetails;

        // Offline / Hiba eset: nincs net, de tudjuk, mit nézett utoljára a user
        // Feltételezzük, hogy van belőle következő rész
        if (apiData == null || lastWatched == null) {
            return "Következő: S" + lastWatched.getSeasonNumber() + " E" + (lastWatched.getEpisodeNumber() + 1) + " (Offline)";
        }

        boolean isCaughtUp = false;

        // Ellenőrizzük, hogy a user utolérte-e az eddig leadott részeket
        if (apiData.getLastEpisodeToAir() != null) {
            if (lastWatched.getSeasonNumber() == apiData.getLastEpisodeToAir().getSeasonNumber() &&
                    lastWatched.getEpisodeNumber() == apiData.getLastEpisodeToAir().getEpisodeNumber()) {
                isCaughtUp = true;
            }
        }

        // döntés és szöveg formázása
        if (isCaughtUp) {
            if (apiData.getNextEpisodeToAir() != null) {
                // UPCOMING állapot (tudjuk, mikor jön a következő)
                int nextSeason = apiData.getNextEpisodeToAir().getSeasonNumber();
                int nextEpisode = apiData.getNextEpisodeToAir().getEpisodeNumber();
                String airDate = apiData.getNextEpisodeToAir().getAirDate();

                return "Érkezik: É" + nextSeason + " E" + nextEpisode + " (" + airDate + ")";
            } else {
                // CAUGHT_UP állapot (Mindent láttunk, és nincs új bejelentve)
                return "Naprakész vagy! (Várakozás bejelentésre)";
            }
        } else {
            // NEXT_EPISODE állapot (van még mit nézni)
            return "Következő: " + lastWatched.getSeasonNumber()+". évad  " + (lastWatched.getEpisodeNumber() + 1)+ ". rész";
        }
    }

}
