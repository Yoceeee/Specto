package com.example.tvandmovies.UI.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.saved.BookmarkFragment;
import com.example.tvandmovies.databinding.ActivityMainBinding;
import com.example.tvandmovies.utilities.FullScreenMode;
import com.example.tvandmovies.UI.home.HomeFragment;
import com.example.tvandmovies.UI.explore.SearchFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private Fragment homeFragment;
    private Fragment searchFragment;
    private Fragment savedItemsFragment;
    private Fragment accountFragment;

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
                // TODO: kibővíteni a többi gombbal
                if (id == R.id.home){
                    selectedFragment = homeFragment;
                } else if (id == R.id.explore) {
                    selectedFragment = searchFragment;
                } else if (id == R.id.bookmark) {
                    selectedFragment = savedItemsFragment;
                }

                if (selectedFragment != null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
            }
        });
    }
}