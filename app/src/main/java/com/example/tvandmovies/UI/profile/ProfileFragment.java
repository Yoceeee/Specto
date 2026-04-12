package com.example.tvandmovies.UI.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tvandmovies.UI.activities.LoadingActivity;
import com.example.tvandmovies.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        loadUserData();
        setupThemeSwitch();
        setupListeners();
    }

    // a bejelentkezett user adatainak betöltése
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        // google vagy email-es módszerrel jelentkezett be a felhasználó
        if(user != null){
            // ellenörzés, hogy Google bejelentkezéssel van-e bent a user
            checkIfGoogleUser(user);

            binding.textUserEmail.setText(user.getEmail());

            // ha rendelkezésre áll username, akkor beállitjuk azt
            if(user.getDisplayName() != null && !user.getDisplayName().isEmpty()){
                binding.textUserName.setText(user.getDisplayName());
            } else {
                binding.textUserName.setText("Vendég");
            }

            // a google-ös bejelentkezés esetén az profilkép betöltése
            if (user.getPhotoUrl() != null) {
                // le kell szedni a Filler képet, hogy látható legyen a google prof kép
                binding.imageProfile.setImageTintList(null);
                binding.imageProfile.setBackground(null);
                binding.imageProfile.setPadding(0, 0, 0, 0);

                String originalUrl = user.getPhotoUrl().toString();

                // Kicseréljük az apró méretet (s96-c) egy nagy, HD méretre (s400-c)
                // A Google URL-ekben ez a "s96-c" jelöli a 96 pixeles méretet kerekítve.
                String highResUrl = originalUrl.replace("s96-c", "s400-c");

                Glide.with(this)
                        .load(highResUrl)
                        .circleCrop() // Kerekítve tölti be
                        .into(binding.imageProfile);
            }
        } else {
            // "vendég" fiókkal lépett be a user
            binding.textUserEmail.setVisibility(View.GONE);
            binding.textUserName.setText("Vendég");
            binding.btnChangePassword.setVisibility(View.GONE);
        }
    }

    // ellenőrzés, hogy a user Google-ös bejelentkezéssel lett-e belépve
    private void checkIfGoogleUser(FirebaseUser user){
        boolean isGoogleUser = false;
        for (UserInfo userInfo : user.getProviderData()){
            if (userInfo.getProviderId().equals("google.com")){
                isGoogleUser = true;
                break;
            }
        }
        if (isGoogleUser) {
            binding.btnChangePassword.setVisibility(View.GONE);
        } else {
            binding.btnChangePassword.setVisibility(View.VISIBLE);
        }
    }

    private void setupThemeSwitch() {
        // Alapállapot beállítása a mentett adatok alapján
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", true); // Alapból legyen sötét
        binding.switchTheme.setChecked(isDarkMode);

        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Elmentjük az új állapotot
            sharedPreferences.edit().putBoolean("isDarkMode", isChecked).apply();

            // Váltunk a témák között
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupListeners(){
        binding.btnLogout.setOnClickListener(view -> {
            signOut();
        });
    }

    // kijelentkezés google fiókos bejelentkezés esetén
    private void signOut() {


        mAuth.signOut(); // kijelentkezés Firebase-ből

        ClearCredentialStateRequest request = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(), // külön háttérszálra megy, hogy ne akassza a fő szálat
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void unused) {
                        Log.d("Logout", "Google Credential sikeresen törölve a háttérben.");
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e("Logout", "Hiba a Credential törlésekor (nem gond, a Firebase már kijelentkezett)", e);
                    }
                }
        );
        navigateToLogin();
    }

    // kijelentkezés után navigáció a bejelentkezési felületre navigál
    private void navigateToLogin() {
        Toast.makeText(requireContext(), "Sikeres kijelentkezés", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), LoadingActivity.class);
        // a vissza gomb előzményének törlése, hogy ne tudjon a user visszanavigálni
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        requireActivity().finish();
    }


    // Memóriaszivárgás megelőzése
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
