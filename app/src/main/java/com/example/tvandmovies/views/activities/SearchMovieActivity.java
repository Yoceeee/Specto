package com.example.tvandmovies.views.activities;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tvandmovies.databinding.ActivitySearchMovieBinding;

public class SearchMovieActivity extends AppCompatActivity {
    private ActivitySearchMovieBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchMovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //a teljes képernyős megjelenítésért felel
        setupWindowFlags();

        // ide jöhet majd a keresés megvalósítása
    }

    private void setupWindowFlags() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

}
