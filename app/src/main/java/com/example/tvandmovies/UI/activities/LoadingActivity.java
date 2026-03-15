package com.example.tvandmovies.UI.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;

import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.ActivityLoadingBinding;
import com.example.tvandmovies.repository.ContentRepository;
import com.example.tvandmovies.utilities.FullScreenMode;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoadingActivity extends AppCompatActivity {
    ActivityLoadingBinding binding; // a binding kiváljta a findViewById-val való keresést
    private CredentialManager credentialManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // a témának megfelelő beállítás
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", true);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // user lekérése bejelentkezés előtt (offline mód miatt)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish(); // Bezárjuk a Login ablakot
            return;
        }

        binding = ActivityLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // beállítja a nézetet az xml fájl alapján

        Glide.with(this)
                .load(R.drawable.boritokep)
                .into(binding.coverImage);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);

        // bejelentkezés / regisztráció emali + jelszóval -> gomb megnyomására továbbvisz az authActivity-re
        binding.loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoadingActivity.this, AuthActivity.class);
            startActivity(intent);
            // Itt NEM hívunk finish()-t, mert megadjuk a lehetőséget arra, hogy a user visszatérjen a fő képernyőre
        });

        // bejelentkezés google auth-al -> elindul a modern azonosítás
        binding.wthGoogleLoginBtn.setOnClickListener(v -> {
            // azonnal tiltom a gombot a legelső kattintáskor, hogy ne lehessen többször bejelentkezést indítani
            binding.wthGoogleLoginBtn.setEnabled(false);
            singInWithGoogle();
        });

       // folytatás vendégként: kattintás hatására továbbvisz a fő kijelzőre
        binding.cntnAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Biztos, ami biztos alapon kiléptetjük az esetleges beragadt Firebase usert
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(LoadingActivity.this, MainActivity.class); // az intent vált az activity-k közt
                startActivity(intent);
            }
        });

        // teljes kijelzős mód
        FullScreenMode.setupWindowFlags(this);
    }

    // google credential-el való bejelentkezés megvalósítása
    private void singInWithGoogle() {
        // kérés felépítése, majd elindítása
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // aszinkron hívás
        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                androidx.core.content.ContextCompat.getMainExecutor(this),
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, androidx.credentials.exceptions.GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInResult(result);
                    }

                    @Override
                    public void onError(androidx.credentials.exceptions.GetCredentialException e) {
                        Toast.makeText(LoadingActivity.this, "Google bejelentkezés hiba.", Toast.LENGTH_SHORT).show();
                        binding.wthGoogleLoginBtn.setEnabled(true);
                    }
                }
        );
    }

    private void handleSignInResult(GetCredentialResponse result) {
        androidx.credentials.Credential credential = result.getCredential();

        if (credential instanceof androidx.credentials.CustomCredential &&
                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential idTokenCredential = GoogleIdTokenCredential.createFrom(((androidx.credentials.CustomCredential) credential).getData());
                firebaseAuthWithGoogle(idTokenCredential.getIdToken());
            } catch (Exception e) {
                Toast.makeText(this, "Hiba a token feldolgozásakor", Toast.LENGTH_SHORT).show();
                binding.wthGoogleLoginBtn.setEnabled(true);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNewUser) {
                                // új user adatainak mentése Firestore-ba is
                                saveUserToFirestore(user.getUid(), user.getEmail(), user.getDisplayName());
                            } else {
                                Toast.makeText(this, "Sikeres bejelentkezés!", Toast.LENGTH_SHORT).show();
                                // szinkronizáció mehet
                                ContentRepository.getInstance(getApplication()).syncFromFirebase();

                                startActivity(new Intent(this, MainActivity.class)); // Tovább az appba
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Sikertelen Firebase-Google összekötés.", Toast.LENGTH_LONG).show();
                        binding.wthGoogleLoginBtn.setEnabled(true);
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String username) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("username", username);
        userData.put("role", "user");
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoadingActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class)); // Tovább az appba
                    finish();
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(LoadingActivity.this, "Sikertelen regisztráció, gond az adatbázisban: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    binding.wthGoogleLoginBtn.setEnabled(false);
                });
    }
}