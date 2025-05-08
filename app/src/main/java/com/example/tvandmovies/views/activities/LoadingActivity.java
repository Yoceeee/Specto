package com.example.tvandmovies.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tvandmovies.databinding.ActivityLoadingBinding;

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
                Intent intent = new Intent(LoadingActivity.this, MovieListActivity.class); // az intent vált az activity-k közt
                    startActivity(intent);
                    // finish(); - ez megakadályozza, hogy vissza lehessen ehhez a nézethez jönni
            }
        });

        // a nézet teljes kijelzőssé tétele (nem lesz sáv a kijelző tetején)
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

}