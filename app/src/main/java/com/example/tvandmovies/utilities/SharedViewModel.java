package com.example.tvandmovies.utilities;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<String> username = new MutableLiveData<>();
    private boolean isFetching = false; // Védelem, hogy ne indítsuk el kétszer

    public SharedViewModel() {
        // Kezdeti érték beállítása null-ra. Így az observe azonnal lefut az első alkalommal!
        username.setValue(null);
    }

    // Ezt hívja meg a Fragment a view.post() belsejéből
    public void loadUsernameIfNeeded() {
        // Ha már van nevünk, vagy épp most töltjük, ne csináljunk semmit!
        if (username.getValue() != null || isFetching) {
            return;
        }

        isFetching = true;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Itt hívódik meg a getInstance(), de a felület ekkor már megrajzolódott!
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("username");
                            // Háttérszálról jövünk, a postValue a biztonságos!
                            username.postValue(name != null && !name.isEmpty() ? name : "Felhasználó");
                        } else {
                            username.postValue("Felhasználó");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SharedViewModel", "Hiba a felhasználónév betöltésekor", e);
                        username.postValue("Felhasználó");
                    });
        } else {
            username.postValue("Vendég");
        }
    }

    public LiveData<String> getUsername() {
        return username;
    }

    public void setUsername(String name) {
        username.setValue(name);
    }
}
