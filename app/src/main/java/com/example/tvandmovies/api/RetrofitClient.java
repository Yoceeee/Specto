package com.example.tvandmovies.api;

import com.example.tvandmovies.model.ApiConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // létrejön egy retrofit példány, amivel lehet API hívásokat kezelni (sablon)
    private static Retrofit retrofit;


    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // az API-ből érkező JSON-t a Gson fogja kezelni
                    .build();
        }
        return retrofit;
    }
}
