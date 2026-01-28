package com.example.tvandmovies.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tvandmovies.api.MovieApi;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.api.ApiConfig;
import com.example.tvandmovies.database.AppDatabase;
import com.example.tvandmovies.database.SavedContentDao;
import com.example.tvandmovies.model.ContentResponse;
import com.example.tvandmovies.model.MediaItem;

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
    private static ContentRepository instance;

    // Cache-elt MutableLiveData-k (osztályszintűek, singletonnal együtt élnek)
    private final MutableLiveData<List<MediaItem>> popularMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> allTimeBestMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> popularSeries = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newSeries = new MutableLiveData<>();

    public static synchronized ContentRepository getInstance(Context context) {
        if(instance == null) instance = new ContentRepository(context);
        return instance;
    }

    private ContentRepository(Context context){
        // retrofit az API-nak
        apiService = RetrofitClient.getClient().create(MovieApi.class);

        // Room adatbázis inicializ
        AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
        savedContentDao = db.savedContentDao();
    }


    // közös, belső függvény, amit az api hívásokhoz használnak az adott tételek
    private void fetchContentIfNeeded(MutableLiveData<List<MediaItem>> liveData, Call<ContentResponse> call){
       if(liveData.getValue() == null){
           call.enqueue(new Callback<ContentResponse>() {
               @Override
               public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                   if (response.isSuccessful() && response.body() != null) {
                       liveData.postValue(response.body().getResults());
                   } else {
                       // TODO: errorMessage  ha kell: liveData.postValue(null);
                   }
               }
               @Override
               public void onFailure(Call<ContentResponse> call, Throwable t) {
                  // TODO: ERROR kezelés
               }
           });
       }
    }


    // --- Publikus API hívások (retrofit)---

    // filmek hívása a HomeFragment kártyáinak
    public LiveData<List<MediaItem>> getPopularMovies(){
        fetchContentIfNeeded(popularMovies, apiService.getPopularMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE));
        return popularMovies;
    }
    public LiveData<List<MediaItem>> getNewMovies() {
        fetchContentIfNeeded(newMovies, apiService.getNewPopularMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE));
        return newMovies;
    }

    public LiveData<List<MediaItem>> getAllTimeBestMovies() {
        fetchContentIfNeeded(allTimeBestMovies, apiService.getAllTimeTopMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE, 1));
        return allTimeBestMovies;
    }

    // Sorozatok hívása a HomeFragment-nek, új és népszerű kategória
    public LiveData<List<MediaItem>> getPopularSeries() {
        fetchContentIfNeeded(popularSeries, apiService.getPopularSeries(ApiConfig.API_KEY, ApiConfig.LANGUAGE));
        return popularSeries;
    }

    public LiveData<List<MediaItem>> getNewSeries() {
        fetchContentIfNeeded(newSeries, apiService.getOnTheAir(ApiConfig.API_KEY, ApiConfig.LANGUAGE));
        return newSeries;
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

    // A régi fetchContent marad a keresőkhöz (új LiveData-t hoz létre)
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


    // ADATBÁZIS MŰVELETEK (Room)

    // content mentése saját listára
    public void insertSavedContent(MediaItem mediaItem){
        AppDatabase.databaseWriteExecutor.execute(() ->{
            savedContentDao.insert(mediaItem);
        });
    }

    // 2. Törlés a kedvencekből
    public void deleteSaved(MediaItem mediaItem) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savedContentDao.delete(mediaItem);
        });
    }

    // 3. Összes mentett elem lekérése
    public LiveData<List<MediaItem>> getAllSaved() {
        return savedContentDao.getAllSavedContent();
    }

    // 4. Ellenőrzés: Mentve van-e az adott tétel?
    public LiveData<MediaItem> getFavoriteById(int id) {
        return savedContentDao.getSavedContentById(id);
    }
}
