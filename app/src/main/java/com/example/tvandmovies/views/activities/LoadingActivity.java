package com.example.tvandmovies.views.activities;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.ActivityLoadingBinding;

public class LoadingActivity extends AppCompatActivity {

    ActivityLoadingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       binding = ActivityLoadingBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoadingActivity.this, MovieListActivity.class);
                    startActivity(intent);
                // finish(); - ez megakadályozza, hogy vissza lehessen ide jönni
            }
        });

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


    }


}