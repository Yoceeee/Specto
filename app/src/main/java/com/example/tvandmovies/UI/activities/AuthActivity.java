package com.example.tvandmovies.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tvandmovies.databinding.ActivityAuthBinding;
import com.example.tvandmovies.repository.ContentRepository;
import com.example.tvandmovies.utilities.FullScreenMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isLoginMode = true; // a bejelentkezés opció a default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase Auth és db inicializálása
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
            binding.btnAuthAction.setEnabled(false);
            handleAuthAction();
        });
    }

    private void handleAuthAction() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki a mezőket!", Toast.LENGTH_SHORT).show();
            binding.btnAuthAction.setEnabled(true);
            return;
        }
        setLoading(true);

        if (!isLoginMode) {
            String username = binding.inputUsername.getText().toString().trim();
            String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();

            // ha bármelyik mező üres maradna, akkor figyelmeztetés és nem megyünk tovább
            if (username.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Kérlek töltsd ki a mezőket!", Toast.LENGTH_SHORT).show();
                setLoading(false);
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
                setLoading(false);
                return;
            }

            if (password.length() < 8){
                Toast.makeText(this, "A jelszónak legalább 8 karakternek kell lennie!", Toast.LENGTH_SHORT).show();
                setLoading(false);
                return;
            }

            // minden mező helyesen lett kitöltve -> regisztáció indulhat
            registerUser(email, username, password);
        }
        else {
            loginUser(email, password);
        }

        // Töltés jelzése
        setLoading(false);
    }

    // regisztráció
    private void registerUser(String email, String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sikeres regisztráció
                        Log.d("AuthActivity", "createUserWithEmail:success");

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // user adatainak mentése adatbázisba
                            saveUserToFirestore(user.getUid(), email, username);
                        }
                        Toast.makeText(AuthActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                    } else {
                        setLoading(false);
                        binding.btnAuthAction.setEnabled(true);
                        // Sikertelen regisztráció
                        Log.w("AuthActivity", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(AuthActivity.this, "Sikertelen regisztráció: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Firestore mentési logika -> új user hozzáadása
    private void saveUserToFirestore(String userId, String email, String username) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("username", username);
        userData.put("role", "user");
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(AuthActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                    navigateToMain(); // mehetünk is a főképernyőre
                })
                .addOnFailureListener(e ->{
                    setLoading(false);
                    Toast.makeText(AuthActivity.this, "Sikertelen regisztráció, gond az adatbázisban: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    binding.btnAuthAction.setEnabled(true);
                });
    }

    // --- BEJELENTKEZÉS ---
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false); // Levesszük a töltést, bármi is lett az eredmény
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Sikeres bejelentkezés!", Toast.LENGTH_SHORT).show();

                        // szinkronizáció mehet
                        ContentRepository.getInstance(getApplication()).syncFromFirebase();
                        navigateToMain(); // Tovább a főoldalra
                    } else {
                        Toast.makeText(AuthActivity.this, "Sikertelen bejelentkezés! Hibás email vagy jelszó.", Toast.LENGTH_LONG).show();
                        binding.btnAuthAction.setEnabled(true);
                    }
                });
    }


    // --- NAVIGÁCIÓ ---
    private void navigateToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Bezárjuk az AuthActivity-t, hogy a vissza gombbal ne lehessen ide visszajönni
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