package com.example.tvandmovies.UI.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.saved.BookmarkFragment;
import com.example.tvandmovies.databinding.ActivityMainBinding;
import com.example.tvandmovies.utilities.FullScreenMode;
import com.example.tvandmovies.UI.home.HomeFragment;
import com.example.tvandmovies.UI.explore.SearchFragment;
import com.example.tvandmovies.UI.profile.ProfileFragment;
import com.example.tvandmovies.worker.SeriesNotificationWorker;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private Fragment homeFragment;
    private Fragment searchFragment;
    private Fragment savedItemsFragment;
    private Fragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // teljes képernyős mód beállítása
        FullScreenMode.setupWindowFlags(this);

        // Fragmentek inicializálása
        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        savedItemsFragment = new BookmarkFragment();
        profileFragment = new ProfileFragment();

        // default: home képernyő betöltése
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
            binding.bottomNavBar.setItemSelected(R.id.home, true);
        }

        // navigációs logika megvalósítása
        binding.bottomNavBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                Fragment selectedFragment = null;
                
                // a 4 db gomb a navbarban
                if (id == R.id.home){
                    selectedFragment = homeFragment;
                } else if (id == R.id.explore) {
                    selectedFragment = searchFragment;
                } else if (id == R.id.bookmark) {
                    selectedFragment = savedItemsFragment;
                }else if (id == R.id.account){
                    selectedFragment = profileFragment;
                }

                if (selectedFragment != null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
            }
        });

        setupNotificationWorker();
    }

    private void setupNotificationWorker() {
        // Értesítési jogosultság kérése
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // WorkManager ütemezése naponta max egyszer
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest seriesCheckRequest =
                new PeriodicWorkRequest.Builder(SeriesNotificationWorker.class, 1, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SeriesDailyCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                seriesCheckRequest
        );
    }
}