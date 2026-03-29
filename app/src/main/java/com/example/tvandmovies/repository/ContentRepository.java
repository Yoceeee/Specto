package com.example.tvandmovies.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tvandmovies.api.MovieApi;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.api.ApiConfig;
import com.example.tvandmovies.database.AppDatabase;
import com.example.tvandmovies.database.SavedContentDao;
import com.example.tvandmovies.database.WatchedEpisodeDao;
import com.example.tvandmovies.model.responses.ContentResponse;
import com.example.tvandmovies.model.responses.CreditsResponse;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.responses.SeasonDetailResponse;
import com.example.tvandmovies.model.responses.TvDetailResponse;
import com.example.tvandmovies.model.entities.WatchedEpisode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.chromium.base.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository: Az egyetlen igazság forrása (SSOT)
 * Kezeli az API hívásokat és a helyi adatbázist
 */
public class ContentRepository {
    private final MovieApi apiService;
    private final SavedContentDao savedContentDao;
    private final WatchedEpisodeDao watchedEpisodeDao;
    private static ContentRepository instance;


    // Cache-elt MutableLiveData-k (osztályszintűek, singletonnal együtt élnek)
    private final MutableLiveData<List<MediaItem>> popularMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> allTimeBestMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> popularSeries = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newSeries = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> trendingMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> trendingSeries = new MutableLiveData<>();

    // hálózati hiba esetére fenntartott lista
    private final MutableLiveData<String> networkError = new MutableLiveData<>();
    public LiveData<String> getNetworkError() { return networkError; }

    public static synchronized ContentRepository getInstance(Context context) {
        if(instance == null) instance = new ContentRepository(context);
        return instance;
    }

    private ContentRepository(Context context){
        // retrofit az API-nak
        apiService = RetrofitClient.getClient().create(MovieApi.class);

        // Room adatbázis inicializ.
        AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
        savedContentDao = db.savedContentDao();
        watchedEpisodeDao = db.watchedEpisodeDao();
    }

    // a bejelentkezett user ID-jat lekérem, ha nincs bejelentkezve, akkor guest lesz
    private String getCurrentUserId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getUid() : "guest";
    }


    // közös, belső függvény, amit az api hívásokhoz használnak az adott tételek
    private void fetchContentIfNeeded(MutableLiveData<List<MediaItem>> liveData, Call<ContentResponse> call, String mediaType){
       if(liveData.getValue() == null){
           call.enqueue(new Callback<ContentResponse>() {
               @Override
               public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                   if (response.isSuccessful() && response.body() != null) {
                       List<MediaItem> items = response.body().getResults();
                       if (mediaType != null){
                           for (MediaItem item : items){
                               item.setMediaType(mediaType);
                           }
                       }
                       liveData.postValue(items);
                   } else {
                       // Ha a szerver hibát dobna
                       liveData.postValue(new ArrayList<>()); // Üres lista, hogy megálljon a töltés
                       networkError.postValue("Hiba a szerver elérésekor.");
                   }
               }
               @Override
               public void onFailure(Call<ContentResponse> call, Throwable t) {
                   liveData.postValue(new ArrayList<>()); // Üres lista, hogy megálljon a töltés
                   networkError.postValue("Nincs internetkapcsolat. Offline mód.");
               }
           });
       }
    }


    // --- Publikus API hívások (retrofit)---

    // filmek hívása a HomeFragment kártyáinak
    public LiveData<List<MediaItem>> getPopularMovies(){
        fetchContentIfNeeded(popularMovies, apiService.getPopularMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE), "movie");
        return popularMovies;
    }
    public LiveData<List<MediaItem>> getNewMovies() {
        fetchContentIfNeeded(newMovies, apiService.getNewPopularMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE), "movie");
        return newMovies;
    }
    public LiveData<List<MediaItem>> getTrendingMovies() {
        fetchContentIfNeeded(trendingMovies, apiService.getTrending("movie", "day", ApiConfig.API_KEY, ApiConfig.LANGUAGE), "movie");
        return trendingMovies;
    }

    public LiveData<List<MediaItem>> getAllTimeBestMovies() {
        fetchContentIfNeeded(allTimeBestMovies, apiService.getAllTimeTopMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE, 1), "movie");
        return allTimeBestMovies;
    }

    // Sorozatok hívása a HomeFragment-nek, új és népszerű kategória
    public LiveData<List<MediaItem>> getPopularSeries() {
        fetchContentIfNeeded(popularSeries, apiService.getPopularSeries(ApiConfig.API_KEY, ApiConfig.LANGUAGE), "series");
        return popularSeries;
    }

    public LiveData<List<MediaItem>> getNewSeries() {
        fetchContentIfNeeded(newSeries, apiService.getOnTheAir(ApiConfig.API_KEY, ApiConfig.LANGUAGE), "series");
        return newSeries;
    }


    // ------ SZEREPLŐK lekérdezés ------
    public LiveData<CreditsResponse> getCredits(int id, String mediaType) {
        MutableLiveData<CreditsResponse> creditsData = new MutableLiveData<>();

        // a megfelelő api hívása, film vagy sorozat típusok közül
        Call<CreditsResponse> call;
        if ("movie".equals(mediaType)) {
            call = apiService.getMovieCredits(id, ApiConfig.API_KEY, ApiConfig.LANGUAGE);
        } else {
            call = apiService.getTvCredits(id, ApiConfig.API_KEY, ApiConfig.LANGUAGE);
        }

        call.enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    creditsData.setValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) {
                creditsData.setValue(null);
            }
        });
        return creditsData;
    }


    // ------ Sorozatok epizódjainak lekérése ------
    public LiveData<SeasonDetailResponse> getSeasonDetails(int seriesId, int seasonNumber) {
        MutableLiveData<SeasonDetailResponse> seasonData = new MutableLiveData<>();

        apiService.getSeasonDetails(seriesId, seasonNumber, ApiConfig.API_KEY, ApiConfig.LANGUAGE)
                .enqueue(new Callback<SeasonDetailResponse>() {
                    @Override
                    public void onResponse(Call<SeasonDetailResponse> call, Response<SeasonDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            seasonData.setValue(response.body());
                        } else {
                            seasonData.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<SeasonDetailResponse> call, Throwable t) {
                        seasonData.setValue(null);
                    }
                });

        return seasonData;
    }

    // Sorozat évadjainak számának lekérése
    public LiveData<Integer> getTvSeasonCount(int seriesId) {
        MutableLiveData<Integer> seasonCount = new MutableLiveData<>();

        apiService.getTvSeriesDetails(seriesId, ApiConfig.API_KEY, ApiConfig.LANGUAGE)
                .enqueue(new Callback<TvDetailResponse>() {
                    @Override
                    public void onResponse(Call<TvDetailResponse> call, Response<TvDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            seasonCount.setValue(response.body().getNumberOfSeasons());
                        }
                    }

                    @Override
                    public void onFailure(Call<TvDetailResponse> call, Throwable t) {
                        seasonCount.setValue(1); // Hiba esetén min. 1 évadot feltételezhetünk
                    }
                });
        return seasonCount;
    }

    // ----- A KERESŐ felület-hez -----

    // Ezek query-függők, így nincs cache – új LiveData mindenkor
    public LiveData<List<MediaItem>> searchMulti(String query) {
        return fetchContent(apiService.searchMulti(query, ApiConfig.API_KEY, ApiConfig.LANGUAGE));
    }
    public LiveData<List<MediaItem>> searchMoviesOnly(String query) {
        return fetchContent(apiService.searchMovie(query, ApiConfig.API_KEY, ApiConfig.LANGUAGE));
    }
    public LiveData<List<MediaItem>> searchTvAndSeriesOnly(String query) {
        return fetchContent(apiService.searchTvAndSeries(query, ApiConfig.API_KEY, ApiConfig.LANGUAGE));
    }
    public LiveData<List<MediaItem>> getTrendingSeries() {
        fetchContentIfNeeded(trendingSeries, apiService.getTrending("tv", "day", ApiConfig.API_KEY, ApiConfig.LANGUAGE), "tv");
        return trendingSeries;
    }

    // A régi fetchContent marad a keresőhöz (új LiveData-t hoz létre)
    private LiveData<List<MediaItem>> fetchContent(Call<ContentResponse> call) {
        MutableLiveData<List<MediaItem>> result = new MutableLiveData<>();

        call.enqueue(new Callback<ContentResponse>() {
            @Override
            public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getResults());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ContentResponse> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }


    // ADATBÁZIS MŰVELETEK (Room & Firebase)

    // content mentése saját listára
    public void insertSavedContent(MediaItem mediaItem){
        String currentUserId = getCurrentUserId();

        // hozzáadjuk a filmhez, hogy melyik userhez tartozik
        mediaItem.setUserId(currentUserId);

        AppDatabase.databaseWriteExecutor.execute(() ->{
            savedContentDao.insert(mediaItem);
        });

        // mentés a Firebase-be (ha be van jelentkezve user)
        if (!currentUserId.equals("guest")){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                .document(currentUserId)
                .collection("saved_content")
                .document(String.valueOf(mediaItem.getId()))
                .set(mediaItem)
                .addOnSuccessListener(aVoid -> Log.d("Sync", "Sikeres mentés a felhőbe."))
                .addOnFailureListener(e -> Log.e("Sync", "Hiba a szinkronizáció közben.", e));
        }
    }

    // a megtekintett epizódok mentése
    public void insertWatchedEpisode(WatchedEpisode watchedEpisode){
        String currentUid = getCurrentUserId();
        watchedEpisode.setUserId(currentUid);
        watchedEpisode.setWatchedAtTimestamp(System.currentTimeMillis());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            watchedEpisodeDao.insertWatchedEpisode(watchedEpisode);
        });

        // mentés firebase-be is, ha van bejelentkezve user
        if (!currentUid.equals("guest")){
            // egyedi id generálás: 131265_S1_E2
            String docId = watchedEpisode.getSeriesId() + "_S" + watchedEpisode.getSeasonNumber() + "_E" + watchedEpisode.getEpisodeNumber();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(currentUid)
                    .collection("watched_episodes")
                    .document(docId)
                    .set(watchedEpisode)
                    .addOnSuccessListener(aVoid -> Log.d("Sync", "Epizód sikeres mentés a felhőbe."))
                    .addOnFailureListener(e -> Log.e("Sync", "Hiba az epizód mentésekor a felhőbe", e));
        }
    }

    // megtekintett epizód törtlése a megtekintett elemek közül
    public void deleteWatchedEpisode(WatchedEpisode watchedEpisode) {
        String currentUid = getCurrentUserId();
        watchedEpisode.setUserId(currentUid);

        // Törlés a lokális adatbázisból
        AppDatabase.databaseWriteExecutor.execute(() -> {
            watchedEpisodeDao.deleteWatchedEpisode(watchedEpisode);
        });

        // Törlés a felhőből (Firebase)
        if (!currentUid.equals("guest")) {
            String docId = watchedEpisode.getSeriesId() + "_S" + watchedEpisode.getSeasonNumber() + "_E" + watchedEpisode.getEpisodeNumber();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUid)
                    .collection("watched_episodes")
                    .document(docId)
                    .delete();
        }
    }

    // az adoptt sorozat összes megnézett epizódjának lekérése
    public LiveData<List<WatchedEpisode>> getAllWatchedForSeries(int seriesId) {
        return watchedEpisodeDao.getAllWatchedForSeries(getCurrentUserId(), seriesId);
    }

    // törlés a kedvencekből
    public void deleteSaved(MediaItem mediaItem) {
        String currentUid = getCurrentUserId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savedContentDao.delete(mediaItem);
        });

        // törlés a Firebase-ből (ha be van jelentkezve user)
        if (!currentUid.equals("guest")){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(currentUid)
                    .collection("saved_content")
                    .document(String.valueOf(mediaItem.getId()))
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("Sync", "Sikeres törlés a felhőből."))
                    .addOnFailureListener(e -> Log.e("Sync", "Hiba törlés közben.", e));
        }
    }

    // Adatok betöltése adatbázisból, ha új eszközre lép a user
    public void syncFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid(); // user id mentése
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("saved_content")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                // Automatikusan visszaalakítja a felhős JSON-t MediaItemmé
                                MediaItem item = doc.toObject(MediaItem.class);
                                if (item != null) {
                                    item.setUserId(uid);
                                    savedContentDao.insert(item); // Beírjuk a lokális Room-ba
                                }
                            }
                        });
                    });
        }
    }

    // Összes mentett content lekérése
    public LiveData<List<MediaItem>> getAllSaved() {
        return savedContentDao.getAllSavedContent(getCurrentUserId());
    }

    // Ellenőrzés: Mentve van-e az adott tétel?
    public LiveData<MediaItem> getFavoriteById(int id) {
        return savedContentDao.getSavedContentById(id, getCurrentUserId());
    }

    // Ezt hívjuk meg, ha a user manuálisan frissít (SwipeRefresh)
    public void forceRefreshData() {
        //Kiürítjük a jelenlegi (esetleg hibás/üres) adatokat
        popularMovies.setValue(null);
        newMovies.setValue(null);
        trendingMovies.setValue(null);

        popularSeries.setValue(null);
        newSeries.setValue(null);

        //Újrahívjuk a letöltő metódusokat (mivel most már null az értékük, a fetchContentIfNeeded újra le fog futni)
        getPopularMovies();
        getPopularSeries();
        getNewMovies();
        getNewSeries();

        getAllTimeBestMovies();
        getTrendingSeries();
        getTrendingMovies();
    }
}
