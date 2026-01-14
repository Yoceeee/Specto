package com.example.tvandmovies.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.tvandmovies.api.MovieApi;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.model.ApiConfig;
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

    public static ContentRepository getInstance() {
        if(instance == null){
            instance = new ContentRepository();
        }
        return instance;
    }

    private ContentRepository(){
        apiService = RetrofitClient.getClient().create(MovieApi.class);
    }

    // segédfüggvény api hívásokhoz, dataTarget kapja a sikeresen lekért listát
    private void fetchContent(Call<ContentResponse> call,
                              MutableLiveData<List<MediaItem>> dataTarget,
                              MutableLiveData<String> errorTarget,
                              Runnable onFinished){
        call.enqueue(new Callback<ContentResponse>() {
            @Override
            public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    dataTarget.setValue(response.body().getResults());
                } else {
                    errorTarget.setValue("Hiba: " + response.code());
                }
                if (onFinished != null){ onFinished.run(); }
            }
            @Override
            public void onFailure(Call<ContentResponse> call, Throwable t) {
                errorTarget.setValue(t.getMessage());
                if (onFinished != null){ onFinished.run(); }
            }
        });
    }

    // --- Publikus API hívások ---
    // filmek hívása
    public void getPopularMovies(MutableLiveData<List<MediaItem>> target, MutableLiveData<String> error, Runnable onFinished) {
        fetchContent(apiService.getPopularMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE), target, error, onFinished);
    }
    public void getNewMovies(MutableLiveData<List<MediaItem>> target, MutableLiveData<String> error, Runnable onFinished) {
        fetchContent(apiService.getNewPopularMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE), target, error, onFinished);
    }

    // sorozatok hívása
    public void getPopularSeries(MutableLiveData<List<MediaItem>> target, MutableLiveData<String> error, Runnable onFinished) {
        fetchContent(apiService.getPopularSeries(ApiConfig.API_KEY, ApiConfig.LANGUAGE), target, error, onFinished);
    }
    public void getNewSeries(MutableLiveData<List<MediaItem>> target, MutableLiveData<String> error, Runnable onFinished) {
        fetchContent(apiService.getAiringTodayTv(ApiConfig.API_KEY, ApiConfig.LANGUAGE), target, error, onFinished);
    }

    // Explore (kereső) felület api hívása, üres Runnable-t adok itt át, mert a Fragment figyeli a LiveData változásait
    public void searchContent(String query, MutableLiveData<List<MediaItem>> target, MutableLiveData<String> error){
        fetchContent(apiService.searchMovie(query, ApiConfig.API_KEY, ApiConfig.LANGUAGE), target, error, () ->{});
    }

}
