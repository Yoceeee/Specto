package com.example.tvandmovies.UI.activities;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.UI.home.HomeViewModel;
import com.example.tvandmovies.databinding.ActivitySeeAllBinding;
import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.utilities.FullScreenMode;

import java.util.ArrayList;
import java.util.List;

public class SeeAllActivity extends AppCompatActivity implements ContentAdapter.ContentClickListener{
    private ActivitySeeAllBinding binding;
    private ContentAdapter gridAdapter;
    private HomeViewModel viewModel;

    private List<MediaItem> currentList = new ArrayList<>(); // a tárolt contenteknek lista

    // az enum tartja nyilván a kiválasztott rendezési feltételt
    private enum SortType {
        DEFAULT, TOP_RATED, NEWEST, ALPHA
    }
    private SortType currentSortType = SortType.DEFAULT; // az alap feltétel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeeAllBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FullScreenMode.setupWindowFlags(this);

        // a kiválasztott kategória adatai
        String category = getIntent().getStringExtra("CATEGORY_TYPE");
        String title = getIntent().getStringExtra("CATEGORY_TITLE");
        binding.categoryTitle.setText(title);

        // Vissza gomb
        binding.btnBack.setOnClickListener(v -> finish());

        // szűrés gomb
        binding.btnSort.setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.CustomBottomSheetDialog);

            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_sort, null);
            bottomSheet.setContentView(sheetView);

            // check circle kikeresése az XML-ből
            android.widget.ImageView checkTop = sheetView.findViewById(R.id.checkTopRated);
            android.widget.ImageView checkNew = sheetView.findViewById(R.id.checkNewest);
            android.widget.ImageView checkAlpha = sheetView.findViewById(R.id.checkAlpha);

            // üres karikák kikeresése
            android.widget.ImageView emptyTop = sheetView.findViewById(R.id.emptyTopRated);
            android.widget.ImageView emptyNew = sheetView.findViewById(R.id.emptyNewest);
            android.widget.ImageView emptyAlpha = sheetView.findViewById(R.id.emptyAlpha);

            // Beállítjuk, hogy csak az a filled circle látszon / ne látszon, ami épp ki van választva
            checkTop.setVisibility(currentSortType == SortType.TOP_RATED ? View.VISIBLE : View.GONE);
            checkNew.setVisibility(currentSortType == SortType.NEWEST ? View.VISIBLE : View.GONE);
            checkAlpha.setVisibility(currentSortType == SortType.ALPHA ? View.VISIBLE : View.GONE);

            emptyTop.setVisibility(currentSortType == SortType.TOP_RATED ? View.GONE : View.VISIBLE);
            emptyNew.setVisibility(currentSortType == SortType.NEWEST ? View.GONE : View.VISIBLE);
            emptyAlpha.setVisibility(currentSortType == SortType.ALPHA ? View.GONE : View.VISIBLE);

            // Legjobbra értékeltek elöl
            sheetView.findViewById(R.id.sortTopRated).setOnClickListener(view -> {
                currentSortType = SortType.TOP_RATED;
                applySortingAndSubmit();
                bottomSheet.dismiss();
            });

            // Legújabbak elöl
            sheetView.findViewById(R.id.sortNewest).setOnClickListener(view -> {
                currentSortType = SortType.NEWEST;
                applySortingAndSubmit();
                bottomSheet.dismiss();
            });

            // A-Z
            sheetView.findViewById(R.id.sortAlpha).setOnClickListener(view -> {
                currentSortType = SortType.ALPHA;
                applySortingAndSubmit();
                bottomSheet.dismiss();
            });

            bottomSheet.show();
        });

        // Adapter beállítása rácsos (Grid) módra
        gridAdapter = new ContentAdapter(this, 2);

        // 3 oszlopos rács (GridLayoutManager)
        binding.rvSeeAll.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvSeeAll.setAdapter(gridAdapter);

        // Adatok betöltése a kategória alapján
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        loadDataForCategory(category);

        // a mentett lista figyelése, adapternek átadása
        viewModel.getAllSaved().observe(this, saved -> {
            if (saved != null) {
                gridAdapter.setSavedItems(saved);
            }
        });
    }

    // a megfelelő rendezés kiválasztása, végrehajtása
    private void applySortingAndSubmit() {
        if (currentList == null || currentList.isEmpty()) return;
        List<MediaItem> sortedList = new ArrayList<>(currentList);

        switch (currentSortType) {
            case TOP_RATED:
                java.util.Collections.sort(sortedList, (item1, item2) ->
                        Double.compare(item2.getVote_avg(), item1.getVote_avg()));
                break;

            case NEWEST:
                java.util.Collections.sort(sortedList, (item1, item2) -> {
                    if (item1.getReDate() == null || item2.getReDate() == null) return 0;
                    return item2.getReDate().compareTo(item1.getReDate()); // Csökkenő (Legújabb elöl)
                });
                break;

            case ALPHA:
                java.util.Collections.sort(sortedList, (item1, item2) -> {
                    String title1 = item1.getTitle() != null ? item1.getTitle() : "";
                    String title2 = item2.getTitle() != null ? item2.getTitle() : "";
                    return title1.compareToIgnoreCase(title2); // Növekvő (A-Z)
                });
                break;

            case DEFAULT:
            default:
                // Ha nincs rendezés, marad az eredeti sorrend
                break;
        }

        // Beküldjük az átrendezett listát az adapterbe
        gridAdapter.submitList(sortedList, () ->{
            // Ez a kód csak akkor fut le, amikor a DiffUtil befejezte a kártyák mozgatását
            if (binding.rvSeeAll != null) {
                binding.rvSeeAll.scrollToPosition(0);
            }
        });
    }

    // a megfelelő kategóriához beállítjuk a megfelelő adaptert
    private void loadDataForCategory(String category){
        if (category == null) return;

        // a kiválasztott kategóriának megfelelő content type beállítása
        if (category.contains("MOVIES")) {
            viewModel.setContentType("movies");
        } else if (category.contains("SERIES")) {
            viewModel.setContentType("series");
        }

        switch (category) {
            case "POPULAR_MOVIES":
                viewModel.getPopularContent().observe(this, items -> {
                    if (items != null) {
                        currentList = new ArrayList<>(items);
                        gridAdapter.submitList(new ArrayList<>(currentList));
                    }
                });
                break;
            case "NEW_MOVIES":
                viewModel.getNewContent().observe(this, items -> {
                    if (items != null) {
                        currentList = new ArrayList<>(items);
                        gridAdapter.submitList(new ArrayList<>(currentList));
                    }
                });
                break;
            case "TOP_RATED_MOVIES":
                viewModel.getAllTimeBestContent().observe(this, items -> {
                    if (items != null) {
                        currentList = new ArrayList<>(items);
                        gridAdapter.submitList(new ArrayList<>(currentList));
                    }
                });
                break;

            // --- SOROZATOK ---
            case "POPULAR_SERIES":
                // Mivel fent átkapcsoltuk "series"-re, a getPopularContent most már a sorozatokat adja!
                viewModel.getPopularContent().observe(this, items -> {
                    if (items != null) {
                        currentList = new ArrayList<>(items);
                        gridAdapter.submitList(new ArrayList<>(currentList));
                    }
                });
                break;
            case "NEW_SERIES":
                viewModel.getNewContent().observe(this, items -> {
                    if (items != null) {
                        currentList = new ArrayList<>(items);
                        gridAdapter.submitList(new ArrayList<>(currentList));
                    }
                });
                break;

            // ---- USER általa mentett listák ----
            case "SAVED_MOVIES":
                viewModel.getAllSaved().observe(this, allSaved -> {
                    if (allSaved != null) {
                        // Csak a filmeket szűrjük ki a mentettek közül
                        List<MediaItem> movies = new ArrayList<>();
                        for (MediaItem item : allSaved) {
                            if ("movie".equals(item.getMediaType())) movies.add(item);
                        }
                        // az eredeti lista mentése
                        currentList = new ArrayList<>(movies);
                        applySortingAndSubmit();
                    }
                });
                break;

            case "SAVED_SERIES":
                viewModel.getAllSaved().observe(this, allSaved -> {
                    if (allSaved != null) {
                        // Csak a sorozatokat szűrjük ki a mentettek közül
                        List<MediaItem> series = new ArrayList<>();
                        for (MediaItem item : allSaved) {
                            if (!"movie".equals(item.getMediaType())) series.add(item);
                        }
                        currentList = new ArrayList<>(series);
                        applySortingAndSubmit();
                    }
                });
                break;
        }
    }

    @Override
    public void onItemClick(MediaItem item) {
        Intent intent = new Intent(this, ActivityContentDetail.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(MediaItem item, boolean isCurrentlySaved) {
        viewModel.toggleSavedStatus(item, isCurrentlySaved);

        if (isCurrentlySaved) {
            Toast.makeText(this, "Eltávolítva a mentett listából", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mentve a kedvencek közé", Toast.LENGTH_SHORT).show();
        }
    }
}
