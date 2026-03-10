package com.example.tvandmovies.UI.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.credentials.CredentialManager;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tvandmovies.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

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
//      setupListeners();
    }

    // a bejelentkezett user adatainak betöltése
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
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
                // le kell szedni a tintet, hogy látható legyen a prof kép
                binding.imageProfile.setImageTintList(null);

                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop() // Kerekítve tölti be
                        .into(binding.imageProfile);
            }
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

    // Memóriaszivárgás megelőzése
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
