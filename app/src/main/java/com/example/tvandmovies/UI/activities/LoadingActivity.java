package com.example.tvandmovies.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tvandmovies.databinding.ActivityLoadingBinding;
import com.example.tvandmovies.utilities.FullScreenMode;

public class LoadingActivity extends AppCompatActivity {

    ActivityLoadingBinding binding; // a binding kiváljta a findViewById-val való keresést

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       binding = ActivityLoadingBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot()); // beállítja a nézetet az xml fájl alapján

       // kattintás hatására továbbvisz a fő kijelzőre
        // TODO: ha már lesz adatbázis, akkor módosítani kell
        binding.cntnAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class); // az intent vált az activity-k közt
                    startActivity(intent);
                    // finish(); - ez megakadályozza, hogy vissza lehessen ehhez a nézethez jönni
            }
        });

        // teljes kijelzős mód
        FullScreenMode.setupWindowFlags(this);
    }

}