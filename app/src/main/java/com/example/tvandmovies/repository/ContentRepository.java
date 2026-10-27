package com.example.tvandmovies.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tvandmovies.UI.saved.EpisodeUiState;
import com.example.tvandmovies.api.TMDbApi;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.api.ApiCall;
import com.example.tvandmovies.api.ApiConfig;
import com.example.tvandmovies.database.AppDatabase;
import com.example.tvandmovies.database.SavedContentDao;
import com.example.tvandmovies.database.SearchHistoryDao;
import com.example.tvandmovies.cloud.CloudPaths;
import com.example.tvandmovies.database.WatchedEpisodeDao;
import com.example.tvandmovies.model.domain.NextEpisodeInfo;
import com.example.tvandmovies.model.entities.CastMember;
import com.example.tvandmovies.model.responses.ActorResponse;
import com.example.tvandmovies.model.responses.ContentResponse;
import com.example.tvandmovies.model.responses.CreditsResponse;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.entities.SearchHistory;
import com.example.tvandmovies.model.responses.OverviewAndTitleResponse;
import com.example.tvandmovies.model.responses.SeasonDetailResponse;
import com.example.tvandmovies.model.responses.TrailerResponse;
import com.example.tvandmovies.model.responses.TvDetailResponse;
import com.example.tvandmovies.model.entities.WatchedEpisode;
import com.example.tvandmovies.utilities.SingleLiveEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.chromium.base.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository: Az egyetlen igazság forrása (SSOT) a user adatai szempontjából, a contentek pedig az "always fresh" megoldással működnek
 *  ami azt jelenti, hogy csak memóriában cache-elem őket, ezért csak internetes kapcsolat mellett jelennek meg
 * Kezeli az API hívásokat és a helyi adatbázist
 */
public class ContentRepository {

    // logoláshoz szükséges tag
    private static final String TAG = "ContentRepository";

    private final TMDbApi apiService;
    private final SavedContentDao savedContentDao;
    private final WatchedEpisodeDao watchedEpisodeDao;
    private final SearchHistoryDao searchHistoryDao;
    private static ContentRepository instance;


    // Cache-elt MutableLiveData-k (osztályszintűek, singletonnal együtt élnek)
    private final MutableLiveData<List<MediaItem>> popularMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> allTimeBestMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> popularSeries = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newSeries = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> trendingMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> trendingSeries = new MutableLiveData<>();
    private final MutableLiveData<List<CastMember>> popularActors = new MutableLiveData<>();

    // hálózati hiba esetére fenntartott lista
    private final SingleLiveEvent<String> networkError = new SingleLiveEvent<>();

    public static synchronized ContentRepository getInstance(Context context) {
        if(instance == null) instance = new ContentRepository(context);
        return instance;
    }

    // trailerek lekérdezése
    public interface TrailerCallback {
        void onResult(String youtubeKey);
    }

    public void getTrailerKey(int id, String mediaType, TrailerCallback callback) {
        String type = "movie".equals(mediaType) ? "movie" : "tv";

        // alapértelmezett (magyar) nyelven
        apiService.getVideos(type, id, null)
                .enqueue(new retrofit2.Callback<TrailerResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<TrailerResponse> call, retrofit2.Response<TrailerResponse> response) {
                        String foundKey = extractTrailerKey(response.body());

                        if (foundKey != null) {
                            callback.onResult(foundKey);
                        } else {
                            // nincs magyar -> jöhet az angol
                            fetchEnglishTrailerFallback(id, type, callback);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<TrailerResponse> call, Throwable t) {
                        fetchEnglishTrailerFallback(id, type, callback);
                    }
                });
    }

    // Segédmetódus az angol nyelvű előzeteshez (filmeknél csak)
    private void fetchEnglishTrailerFallback(int id, String type, TrailerCallback callback) {
        apiService.getVideos(type, id, "en-US")
                .enqueue(new retrofit2.Callback<TrailerResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<TrailerResponse> call, retrofit2.Response<TrailerResponse> response) {
                        String foundKey = extractTrailerKey(response.body());

                        if (foundKey != null) {
                            callback.onResult(foundKey);
                        } else if ("tv".equals(type)) {
                            // csak sorozatoknál: 1. évad angol előzetese
                            fetchSeasonOneTrailerFallback(id, callback);
                        } else {
                            callback.onResult(null);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<TrailerResponse> call, Throwable t) {
                        if ("tv".equals(type)) {
                            fetchSeasonOneTrailerFallback(id, callback);
                        } else {
                            callback.onResult(null);
                        }
                    }
                });
    }

    // Segédmetódus: 1. évad angol előzetese (csak sorozatnál)
    private void fetchSeasonOneTrailerFallback(int seriesId, TrailerCallback callback) {
        apiService.getSeasonVideos(seriesId, 1, "en-US")
                .enqueue(new retrofit2.Callback<TrailerResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<TrailerResponse> call, retrofit2.Response<TrailerResponse> response) {
                        callback.onResult(extractTrailerKey(response.body()));
                    }

                    @Override
                    public void onFailure(retrofit2.Call<TrailerResponse> call, Throwable t) {
                        callback.onResult(null);
                    }
                });
    }

    private String extractTrailerKey(TrailerResponse responseBody) {
        if (responseBody != null && responseBody.getResults() != null) {
            for (TrailerResponse.VideoResult video : responseBody.getResults()) {
                if ("YouTube".equalsIgnoreCase(video.getSite()) &&
                        ("Trailer".equalsIgnoreCase(video.getType()) || "Teaser".equalsIgnoreCase(video.getType()))) {
                    return video.getKey();
                }
            }
        }
        return null;
    }


    // a sorozat legutobb megtekintett epizódja szerint lekéri a lehetséges következő rész állapotát
    public LiveData<NextEpisodeInfo> getNextEpisodeRawData(int seriesId){
        MutableLiveData<NextEpisodeInfo> result = new MutableLiveData<>();

        AppDatabase.databaseWriteExecutor.execute(() ->{
            WatchedEpisode last = watchedEpisodeDao.getLastWatchedEpisodeSyncObj(getCurrentUserId(), seriesId);

            try{
                // szimplán le lesz kérve az adat, a konkrét epizódról
                Response<TvDetailResponse> response = apiService.getTvSeriesDetails(seriesId).execute();
                if (response.isSuccessful()){
                    result.postValue(new NextEpisodeInfo(last, response.body()));
                } else {
                    result.postValue(new NextEpisodeInfo(last, null));
                }
            }catch (IOException ex){
                result.postValue(new NextEpisodeInfo(last,null));
            }
        });
        return result;
    }

    private ContentRepository(Context context){
        // retrofit az API-nak
        apiService = RetrofitClient.getClient().create(TMDbApi.class);

        // Room adatbázis inicializ.
        AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
        savedContentDao = db.savedContentDao();
        watchedEpisodeDao = db.watchedEpisodeDao();
        searchHistoryDao = db.searchHistoryDao();
    }

    // a bejelentkezett user ID-jat lekérem, ha nincs bejelentkezve, akkor guest lesz
    private String getCurrentUserId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getUid() : "guest";
    }

    // közös, belső függvény, amit az API hívásokhoz használnak az adott tételek
    private void fetchContentIfNeeded(MutableLiveData<List<MediaItem>> liveData, Call<ContentResponse> call, String mediaType){
        List<MediaItem> currentData = liveData.getValue();
        if (currentData != null && !currentData.isEmpty()) {
            Log.d(TAG, "Az adat már a memóriában van, a letöltést kihagyjuk: " + mediaType);
            return;
        }

        if (liveData.getValue() == null || liveData.getValue().isEmpty()){
           call.enqueue(new Callback<ContentResponse>() {
               @Override
               public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                   if (response.isSuccessful() && response.body() != null) {
                       List<MediaItem> items = safeResults(response.body());
                       if (mediaType != null){
                           for (MediaItem item : items){
                               item.setMediaType(mediaType);
                           }
                       }
                       Log.i(TAG, "Sikeres művelet! Típus: " + mediaType + " | A kapott lista mérete: " + items.size());
                       liveData.setValue(items);
                   } else {
                       // Ha a szerver hibát dobna
                       liveData.setValue(new ArrayList<>()); // üres lista, hogy megálljon a töltés
                       networkError.setValue("Hiba a szerver elérésekor.");
                       Log.e(TAG, "Kritikus hiba történt a szerver elérése során!");
                   }
               }
               @Override
               public void onFailure(Call<ContentResponse> call, Throwable t) {
                   liveData.setValue(new ArrayList<>()); // üres lista, hogy megálljon a töltés
                   networkError.setValue("Nincs internetkapcsolat. Offline mód.");
                   Log.e(TAG, "Kritikus hiba történt a szerver elérése során!" + t);
               }
           });
       }
    }


    // a válasz eredménylistája, null esetén üres lista (nem dobunk NullPointerException-t)
    private static List<MediaItem> safeResults(ContentResponse body) {
        List<MediaItem> results = body.getResults();
        return results != null ? results : new ArrayList<>();
    }

    // --- Publikus API hívások (retrofit)---

    // filmek hívása a HomeFragment kártyáinak
    public LiveData<List<MediaItem>> getPopularMovies(){
        Log.d(TAG, "Elindult a népszerű filmek lekérése.");
        fetchContentIfNeeded(popularMovies, apiService.getPopularMovies(), "movie");
        return popularMovies;
    }
    public LiveData<List<MediaItem>> getNewMovies() {
        fetchContentIfNeeded(newMovies, apiService.getNewPopularMovies(), "movie");
        return newMovies;
    }
    public LiveData<List<MediaItem>> getTrendingMovies() {
        fetchContentIfNeeded(trendingMovies, apiService.getTrending("movie", "week"), "movie");
        return trendingMovies;
    }

    public LiveData<List<MediaItem>> getAllTimeBestMovies() {
        fetchContentIfNeeded(allTimeBestMovies, apiService.getAllTimeTopMovies(1), "movie");
        return allTimeBestMovies;
    }

    // Sorozatok hívása a HomeFragment-nek, új és népszerű kategória
    public LiveData<List<MediaItem>> getPopularSeries() {
        fetchContentIfNeeded(popularSeries, apiService.getPopularSeries(), "tv");
        return popularSeries;
    }

    public LiveData<List<MediaItem>> getNewSeries() {
        fetchContentIfNeeded(newSeries, apiService.getAiringTodaySeries(), "tv");
        return newSeries;
    }

    // a kiválaszott tartalomhoz hasonló contentek lekérdezése
    public LiveData<List<MediaItem>> getRecommendations(int id, String mediaType) {
        MutableLiveData<List<MediaItem>> data = new MutableLiveData<>();
        apiService.getRecommendations(mediaType, id).enqueue(new Callback<ContentResponse>() {
            @Override
            public void onResponse(@NonNull Call<ContentResponse> call, @NonNull Response<ContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.postValue(response.body().getResults());
                } else {
                    data.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ContentResponse> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    // Kifejezetten angol leírás lekérése, ha nem elérhető magyar fordítás
    public LiveData<OverviewAndTitleResponse> getEnglishFallback(int id, String mediaType) {
        MutableLiveData<OverviewAndTitleResponse> resultLiveData = new MutableLiveData<>();

        retrofit2.Callback<OverviewAndTitleResponse> callback = new retrofit2.Callback<OverviewAndTitleResponse>() {
            @Override
            public void onResponse(Call<OverviewAndTitleResponse> call, retrofit2.Response<OverviewAndTitleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultLiveData.setValue(response.body());
                } else {
                    resultLiveData.setValue(null);
                }
            }
            @Override
            public void onFailure(Call<OverviewAndTitleResponse> call, Throwable t) {
                resultLiveData.setValue(null);
            }
        };

        if ("movie".equals(mediaType)) {
            apiService.getMovieOverviewAndTitle(id).enqueue(callback);
        } else {
            apiService.getTvOverviewAndTitle(id).enqueue(callback);
        }

        return resultLiveData;
    }


    // ------ SZEREPLŐK lekérdezés ------
    public LiveData<CreditsResponse> getCredits(int id, String mediaType) {
        MutableLiveData<CreditsResponse> creditsData = new MutableLiveData<>();

        // a megfelelő api hívása, film vagy sorozat típusok közül
        Call<CreditsResponse> call;
        if ("movie".equals(mediaType)) {
            call = apiService.getMovieCredits(id);
        } else {
            call = apiService.getTvCredits(id);
        }

        ApiCall.enqueue(call, creditsData);
        return creditsData;
    }


    // ------ Sorozatok epizódjainak lekérése ------
    public LiveData<SeasonDetailResponse> getSeasonDetails(int seriesId, int seasonNumber) {
        MutableLiveData<SeasonDetailResponse> seasonData = new MutableLiveData<>();

        apiService.getSeasonDetails(seriesId, seasonNumber)
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

    // Sorozat évadjainak mennyiségét adja vissza
    public LiveData<Integer> getTvSeasonCount(int seriesId) {
        MutableLiveData<Integer> seasonCount = new MutableLiveData<>();

        apiService.getTvSeriesDetails(seriesId)
                .enqueue(new Callback<TvDetailResponse>() {
                    @Override
                    public void onResponse(Call<TvDetailResponse> call, Response<TvDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            seasonCount.setValue(response.body().getNumberOfSeasons());
                        } else {
                            seasonCount.setValue(1); // hibás válasznál is min. 1 évadot feltételezünk
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

    // Ezek query-függők, így nincs cache – új LiveData mindig
    public LiveData<List<MediaItem>> searchMulti(String query) {
        return fetchContent(apiService.searchMulti(query), null);
    }
    public LiveData<List<MediaItem>> searchMoviesOnly(String query) {
        return fetchContent(apiService.searchMovie(query), "movie");
    }
    public LiveData<List<MediaItem>> searchTvAndSeriesOnly(String query) {
        return fetchContent(apiService.searchTvAndSeries(query), "tv");
    }

    public LiveData<List<MediaItem>> getTrendingSeries() {
        fetchContentIfNeeded(trendingSeries, apiService.getTrending("tv", "day"), "tv");
        return trendingSeries;
    }

    // a népszerű színészek lekérdezése
    public LiveData<List<CastMember>> getPopularActors() {
        if (popularActors.getValue() == null || popularActors.getValue().isEmpty()) {
            apiService.getPopularActors().enqueue(new Callback<ActorResponse>() {
                @Override
                public void onResponse(Call<ActorResponse> call, Response<ActorResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        popularActors.setValue(response.body().getResults());
                    }
                }

                @Override
                public void onFailure(Call<ActorResponse> call, Throwable t) {
                    popularActors.setValue(new ArrayList<>());
                }
            });
        }
        return popularActors;
    }


    // A régi fetchContent marad a keresőhöz (új LiveData-t hoz létre)
    private LiveData<List<MediaItem>> fetchContent(Call<ContentResponse> call, String forcedMediaType) {
        MutableLiveData<List<MediaItem>> result = new MutableLiveData<>();

        call.enqueue(new Callback<ContentResponse>() {
            @Override
            public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MediaItem> items = safeResults(response.body());
                    if (forcedMediaType != null) {
                        for (MediaItem item : items) {
                            item.setMediaType(forcedMediaType);
                        }
                    }
                    result.setValue(items);
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

    // a profil nézet számára szolgáltatja a megtekintett tartalmak mennyiségét
    public LiveData<Integer> getWatchedMoviesCount() {
        return savedContentDao.getWatchedMoviesCount(getCurrentUserId());
    }

    public LiveData<Integer> getWatchedSeriesCount() {
        return savedContentDao.getWatchedSeriesCount(getCurrentUserId());
    }

    public LiveData<List<MediaItem>> getNotifiedSeries() {
        return savedContentDao.getNotifiedSeries(getCurrentUserId());
    }


    // ---- ADATBÁZIS MŰVELETEK (Room & Firebase) ----

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
            CloudPaths.savedContent(currentUserId, mediaItem.getId())
                .set(mediaItem)
                .addOnSuccessListener(aVoid -> Log.d("Sync", "Sikeres mentés a felhőbe."))
                .addOnFailureListener(e -> Log.e("Sync", "Hiba a szinkronizáció közben.", e));
        }
    }

    // a megtekintett epizódok mentése
    public void insertWatchedEpisode(WatchedEpisode watchedEpisode, MediaItem seriesItem) {
        String currentUid = getCurrentUserId();

        // Háttérszál indítása
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Megnézzük szinkron módon, hogy a sorozat benne van-e már a mentettek között
            MediaItem existingItem = savedContentDao.getSavedContentByIdSync(seriesItem.getId(), currentUid);

            // Ha nincs elmentve, automatikusan hozzáadjuk a mentett listához (helyben és felhőben is)
            if (existingItem == null) {
                seriesItem.setUserId(currentUid);
                savedContentDao.insert(seriesItem);

                if (!currentUid.equals("guest")) {
                    CloudPaths.savedContent(currentUid, seriesItem.getId())
                            .set(seriesItem);
                }
            }

            // ezt követően mentjük magát a megnézett epizódot a saját táblájába
            watchedEpisode.setUserId(currentUid);
            watchedEpisode.setWatchedAtTimestamp(System.currentTimeMillis());
            watchedEpisodeDao.insertWatchedEpisode(watchedEpisode);

            // mentés firebase-be is, ha van bejelentkezve user
            if (!currentUid.equals("guest")) {
                // egyedi id generálás: 131265_S1_E2
                String docId = CloudPaths.watchedEpisodeDocId(watchedEpisode.getSeriesId(), watchedEpisode.getSeasonNumber(), watchedEpisode.getEpisodeNumber());
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUid)
                        .collection("watched_episodes")
                        .document(docId)
                        .set(watchedEpisode)
                        .addOnSuccessListener(aVoid -> Log.d("Sync", "Epizód sikeres mentés a felhőbe."))
                        .addOnFailureListener(e -> Log.e("Sync", "Hiba az epizód mentésekor a felhőbe", e));
            }

            // Ellenőrizzük, hogy ez volt-e az utolsó epizód
            try {
                retrofit2.Response<TvDetailResponse> response = apiService.getTvSeriesDetails(seriesItem.getId()).execute();
                if (response.isSuccessful() && response.body() != null) {
                    TvDetailResponse details = response.body();
                    TvDetailResponse.EpisodeAirInfo lastEpisode = details.getLastEpisodeToAir();

                    if (lastEpisode != null &&
                            watchedEpisode.getSeasonNumber() == lastEpisode.getSeasonNumber() &&
                            watchedEpisode.getEpisodeNumber() == lastEpisode.getEpisodeNumber()) {

                        // Ez az utolsó epizód -> megnézettnek jelölve a sorozat
                        seriesItem.setWatched(true);
                        seriesItem.setUserId(currentUid);
                        savedContentDao.insert(seriesItem);

                        if (!currentUid.equals("guest")) {
                            CloudPaths.savedContent(currentUid, seriesItem.getId())
                                    .set(seriesItem);
                        }
                        Log.d(TAG, "Sorozat befejezve: " + seriesItem.getTitle());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Hiba az utolsó epizód ellenőrzésekor", e);
            }
        });
    }

    // megtekintett epizód törtlése a megtekintett elemek közül
    public void deleteWatchedEpisode(WatchedEpisode watchedEpisode) {
        String currentUid = getCurrentUserId();
        watchedEpisode.setUserId(currentUid);

        // Törlés a lokális adatbázisból
        AppDatabase.databaseWriteExecutor.execute(() -> {
            watchedEpisodeDao.deleteWatchedEpisode(watchedEpisode);
        });

        // Törlés a Firebase-ből
        if (!currentUid.equals("guest")) {
            String docId = CloudPaths.watchedEpisodeDocId(watchedEpisode.getSeriesId(), watchedEpisode.getSeasonNumber(), watchedEpisode.getEpisodeNumber());
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

    // a legutoljára megnézett epizód lekérése
    public LiveData<WatchedEpisode> getLastWatchedEpisode(int seriesId) {
        return watchedEpisodeDao.getLastWatchedEpisodeLive(getCurrentUserId(), seriesId);
    }

    // törlés a mentett elemek közül
    public void deleteSaved(MediaItem mediaItem) {
        String currentUid = getCurrentUserId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savedContentDao.delete(mediaItem);
        });

        // törlés a Firebase-ből (ha be van jelentkezve user)
        if (!currentUid.equals("guest")){
            CloudPaths.savedContent(currentUid, mediaItem.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("Sync", "Sikeres törlés a felhőből."))
                    .addOnFailureListener(e -> Log.e("Sync", "Hiba törlés közben.", e));
        }
    }

    // Adatok betöltése adatbázisból, bejelentkezés során
    public void syncFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Mentett tartalmak szinkronizálása
            db.collection("users")
                    .document(uid)
                    .collection("saved_content")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                MediaItem item = doc.toObject(MediaItem.class);
                                if (item != null) {
                                    item.setUserId(uid);
                                    savedContentDao.insert(item);
                                }
                            }
                        });
                    });

            // Megtekintett epizódok (Folyamatban lévő sorozatok) szinkronizálása
            db.collection("users")
                    .document(uid)
                    .collection("watched_episodes")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            java.util.Set<Integer> uniqueSeriesIds = new java.util.HashSet<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                WatchedEpisode episode = doc.toObject(WatchedEpisode.class);
                                if (episode != null) {
                                    episode.setUserId(uid);
                                    watchedEpisodeDao.insertWatchedEpisode(episode);
                                    uniqueSeriesIds.add(episode.getSeriesId());
                                }
                            }
                            // Metaadatok pótlása a "Folytatás" listához, ha hiányoznak
                            for (Integer seriesId : uniqueSeriesIds) {
                                if (savedContentDao.getSavedContentByIdSync(seriesId, uid) == null) {
                                    fetchAndSaveSeriesMetadataSync(seriesId, uid);
                                }
                            }
                        });
                    });

            // keresési előzmények szinkronizálása
            db.collection("users")
                    .document(uid)
                    .collection("search_history")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                SearchHistory history = doc.toObject(SearchHistory.class);
                                if (history != null) {
                                    history.setUserId(uid);
                                    searchHistoryDao.insertHistoryItem(history);
                                }
                            }
                        });
                    });
        }
    }

    // Segédmetódus a hiányzó sorozat metaadatok pótlásához szinkronizáláskor
    private void fetchAndSaveSeriesMetadataSync(int seriesId, String uid) {
        try {
            Response<TvDetailResponse> response = apiService.getTvSeriesDetails(seriesId).execute();
            if (response.isSuccessful() && response.body() != null) {
                TvDetailResponse details = response.body();
                MediaItem item = new MediaItem();
                item.setId(details.getId());
                item.setTitle(details.getName());
                item.setPosterUrl(details.getPosterPath());
                item.setBackdropUrl(details.getBackdropPath());
                item.setMediaType("tv");
                item.setUserId(uid);
                item.setVote_avg(details.getVoteAverage());
                item.setVote_count(details.getVoteCount());
                item.setDescription(details.getOverview());
                item.setReDate(null);
                
                savedContentDao.insert(item);
                
                // Szinkronizáljuk vissza Firebase-be, hogy a többi eszköz is lássa a metaadatot
                CloudPaths.savedContent(uid, item.getId())
                        .set(item)
                        .addOnSuccessListener(aVoid -> android.util.Log.d(TAG, "Sync: Metaadat feltöltve Firebase-be: " + item.getTitle()))
                        .addOnFailureListener(e -> android.util.Log.e(TAG, "Sync: Hiba a metaadat feltöltésekor", e));

                android.util.Log.d(TAG, "Sync: Pótolva a hiányzó sorozat metaadat: " + item.getTitle());
            }
        } catch (IOException e) {
            android.util.Log.e(TAG, "Sync: Hiba a metaadat pótlásakor: " + seriesId, e);
        }
    }

    // Összes mentett content lekérése
    public LiveData<List<MediaItem>> getAllSaved() {
        return savedContentDao.getAllSavedContent(getCurrentUserId());
    }

    // Folyamatban lévő sorozatok lekérése (Belső JOIN a Room-ban)
    public LiveData<List<MediaItem>> getContinueWatchingSeries() {
        return savedContentDao.getContinueWatchingSeries(getCurrentUserId());
    }

    // Mentve van-e az adott content?
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
        trendingSeries.setValue(null);
        allTimeBestMovies.setValue(null);

        //Újrahívjuk a letöltő metódusokat (mivel most már null az értékük, a fetchContentIfNeeded újra le fog futni)
        getPopularMovies();
        getPopularSeries();
        getNewMovies();
        getNewSeries();

        getAllTimeBestMovies();
        getTrendingSeries();
        getTrendingMovies();
    }


    // --- KERESÉSI ELŐZMÉNYEK ---
    public void addToHistory(MediaItem item) {
        String currentUid = getCurrentUserId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            SearchHistory historyItem = new SearchHistory(
                    item.getId(),
                    currentUid,
                    item.getTitle(),
                    item.getPosterUrl(),
                    item.getMediaType(),
                    item.getVote_avg(),
                    item.getReDate(),
                    item.getGenreIds(),
                    System.currentTimeMillis()
            );
            searchHistoryDao.insertHistoryItem(historyItem);

            // mentés firebase-be is, ha van bejelentkezve user, illetve rendelkezésre áll internet
            if (!currentUid.equals("guest")){
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(currentUid)
                        .collection("search_history")
                        .document(String.valueOf(historyItem.getId()))
                        .set(historyItem)
                        .addOnSuccessListener(aVoid -> Log.d("Sync", "Keresési előzmény sikeresen mentve a felhőbe."))
                        .addOnFailureListener(e -> Log.e("Sync", "Hiba az előzmény mentésekor.", e));
            }
        });
    }

    public LiveData<List<SearchHistory>> getRecentHistory() {
        return searchHistoryDao.getRecentHistory(getCurrentUserId());
    }

    public void clearAllHistory() {
        String currentUid = getCurrentUserId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            searchHistoryDao.clearAllHistory(currentUid);

            if (!currentUid.equals("guest")) {
                // Firebase-ben a kollekciók törlése bonyolultabb (kliens oldalon nem lehet egyben),
                // de egy egyszerűbb megközelítés a dokumentumok lekérése és törlése:
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUid)
                        .collection("search_history")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete();
                            }
                        });
            }
        });
    }
}
