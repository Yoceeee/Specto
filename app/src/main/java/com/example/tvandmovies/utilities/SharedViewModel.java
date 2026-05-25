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
    private MutableLiveData<String> photoUrl = new MutableLiveData<>();
    private boolean isFetching = false;

    public SharedViewModel() {}

    // Ezt hívja meg a Fragment a view.post() belsejéből
    public void loadUsernameIfNeeded() {
        if (username.getValue() != null || isFetching) {
            return;
        }

        isFetching = true;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        isFetching = false;
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("username");
                            String photo = documentSnapshot.getString("photoUrl");
                            
                            username.postValue(name != null && !name.isEmpty() ? name : (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Felhasználó"));
                            
                            // Ha a Firestore-ban nincs kép, de az Auth-ban van (pl. Google), használjuk azt
                            if ((photo == null || photo.isEmpty()) && currentUser.getPhotoUrl() != null) {
                                photoUrl.postValue(currentUser.getPhotoUrl().toString());
                            } else {
                                photoUrl.postValue(photo);
                            }
                        } else {
                            username.postValue(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Felhasználó");
                            if (currentUser.getPhotoUrl() != null) {
                                photoUrl.postValue(currentUser.getPhotoUrl().toString());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        isFetching = false;
                        Log.e("SharedViewModel", "Hiba a felhasználónév betöltésekor", e);
                        username.postValue("Felhasználó");
                    });
        } else {
            isFetching = false;
            username.postValue("Vendég");
            photoUrl.postValue(null);
        }
    }

    public LiveData<String> getUsername() {
        return username;
    }

    public LiveData<String> getPhotoUrl() {
        return photoUrl;
    }

    public void setUsername(String name) {
        username.setValue(name);
    }

    public void setPhotoUrl(String url) {
        photoUrl.setValue(url);
    }
}
