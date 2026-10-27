package com.example.tvandmovies.api;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Egységes segédmetódus Retrofit-hívásokhoz, amelyek egy LiveData-ba töltik az eredményt.
 *
 * Garantálja, hogy a célként megadott LiveData minden esetben emittál:
 * sikeres válasznál a body-t, sikertelen válasznál vagy hálózati hibánál null-t.
 * Így a UI sosem marad végtelen várakozásban.
 */
public final class ApiCall {

    private static final String TAG = "ApiCall";

    private ApiCall() {}

    public static <T> void enqueue(Call<T> call, MutableLiveData<T> target) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> c, Response<T> response) {
                if (response.isSuccessful()) {
                    target.setValue(response.body());
                } else {
                    Log.w(TAG, "Sikertelen válasz, HTTP " + response.code());
                    target.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<T> c, Throwable t) {
                Log.w(TAG, "Hálózati hiba", t);
                target.setValue(null);
            }
        });
    }
}
