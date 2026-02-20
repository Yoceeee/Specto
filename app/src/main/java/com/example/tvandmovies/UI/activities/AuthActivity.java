package com.example.tvandmovies.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tvandmovies.databinding.ActivityAuthBinding;
import com.example.tvandmovies.utilities.FullScreenMode; // Ha használod

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private boolean isLoginMode = true; // a bejelentkezés opció a default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Full screen
        FullScreenMode.setupWindowFlags(this);
        setupUI();
        setupListeners();
    }

    // a megfeleő felirat beállítása
    private void setupUI() {
        if (isLoginMode) {
            binding.textTitle.setText("Üdv újra!");
            binding.textSubtitle.setText("Jelentkezz be a folytatáshoz");
            binding.btnAuthAction.setText("Bejelentkezés");
            binding.textSwitchInfo.setText("Még nincs fiókod?");
            binding.textSwitchAction.setText("Regisztráció");

            //a regisztrációs mezők eltávolítása
            binding.inputConfirmPasswordLayout.setVisibility(View.GONE);
            binding.inputConfirmUsernameLayout.setVisibility(View.GONE);
        } else {
            binding.textTitle.setText("Fiók létrehozása");
            binding.textSubtitle.setText("Add meg az adataidat a regisztrációhoz");
            binding.btnAuthAction.setText("Regisztráció");
            binding.textSwitchInfo.setText("Már van fiókod?");
            binding.textSwitchAction.setText("Bejelentkezés");

            binding.inputConfirmPasswordLayout.setVisibility(View.VISIBLE);
            binding.inputConfirmUsernameLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        // Vissza gomb
        binding.btnBack.setOnClickListener(v -> finish());

        // Váltás Login/Regisztráció között
        binding.textSwitchAction.setOnClickListener(v -> {
            isLoginMode = !isLoginMode; // Átfordítjuk az állapotot
            setupUI(); // Frissítjük a felületet
        });

        // A Fő Gomb (Login vagy Regisztráció)
        binding.btnAuthAction.setOnClickListener(v -> {
            handleAuthAction();
        });
    }

    private void handleAuthAction() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki a mezőket!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Töltés jelzése
        setLoading(true);

        // ITT LESZ MAJD A FIREBASE LOGIKA
        // Most csak szimuláljuk
        new android.os.Handler().postDelayed(() -> {
            setLoading(false);
            if (isLoginMode) {
                // Ha sikeres a login, megyünk a főoldalra
                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                // Töröljük a back stacket, hogy vissza gombbal ne jöjjön ide
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // Ha sikeres a regisztráció
                Toast.makeText(this, "Regisztráció sikeres!", Toast.LENGTH_SHORT).show();
                // Átváltunk loginra vagy beléptetjük
            }
        }, 1500);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnAuthAction.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnAuthAction.setVisibility(View.VISIBLE);
        }
    }
}