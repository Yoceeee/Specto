package com.example.tvandmovies.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.ActivityLoadingBinding;
import com.example.tvandmovies.utilities.FullScreenMode;
import com.google.firebase.auth.FirebaseAuth;
import com.bumptech.glide.Glide;

public class LoadingActivity extends AppCompatActivity {

    ActivityLoadingBinding binding; // a binding kiváljta a findViewById-val való keresést

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       binding = ActivityLoadingBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot()); // beállítja a nézetet az xml fájl alapján

        Glide.with(this)
                .load(R.drawable.boritokep)
                .into(binding.coverImage);

        // bejelentkezés gomb megnyomására továbbvisz az authActivity-re
        binding.loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoadingActivity.this, AuthActivity.class);
            startActivity(intent);
            // Itt NEM hívunk finish()-t, mert vissza akarhat jönni a user a fő képernyőre
        });

       // kattintás hatására továbbvisz a fő kijelzőre
        binding.cntnAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Biztos, ami biztos alapon kiléptetjük a beragadt Firebase usert
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(LoadingActivity.this, MainActivity.class); // az intent vált az activity-k közt
                startActivity(intent);
                finish();
            }
        });

        // teljes kijelzős mód
        FullScreenMode.setupWindowFlags(this);
    }
}