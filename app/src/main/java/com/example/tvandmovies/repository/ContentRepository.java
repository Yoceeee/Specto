package com.example.tvandmovies.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tvandmovies.api.MovieApi;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.api.ApiConfig;
import com.example.tvandmovies.model.ContentResponse;
import com.example.tvandmovies.model.MediaItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository: Kizárólag az adatforrásokkal foglalkozik (API, Adatbázis).
 * Nem tud semmit a UI-ról (Fragmentekről).
 */
public class ContentRepository {
    private final MovieApi apiService;
    private static ContentRepository instance;

    // Cache-elt MutableLiveData-k (osztályszintűek, singletonnal együtt élnek)
    private final MutableLiveData<List<MediaItem>> popularMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> allTimeBestMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> popularSeries = new MutableLiveData<>();
    private final MutableLiveData<List<MediaItem>> newSeries = new MutableLiveData<>();

    public static synchronized ContentRepository getInstance() {
        if(instance == null) instance = new ContentRepository();
        return instance;
    }

    private ContentRepository(){
        apiService = RetrofitClient.getClient().create(MovieApi.class);
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

    // --- Publikus API hívások ---

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


    // ----- KERESŐ felület-hez -----

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
}
