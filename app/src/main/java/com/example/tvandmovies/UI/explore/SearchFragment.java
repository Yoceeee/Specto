package com.example.tvandmovies.UI.explore;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ÚJ IMPORT
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.FragmentSearchBinding;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.entities.SearchHistory;
import com.example.tvandmovies.UI.activities.ActivityContentDetail;
import com.example.tvandmovies.UI.adapter.ContentAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements ContentAdapter.ContentClickListener {
    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private ContentAdapter contentAdapter;

    // Keresés késleltetése (Debounce)
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstance){
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adapter inicializálás: átadjuk a 'this'-t mint listenert
        contentAdapter = new ContentAdapter(this, 1);
        setupRecyclerView(binding.recyclerView, contentAdapter);

        // ViewModel példányosítása
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Feliratkozás az adatokra (Observer)
        observeViewModel();

        // Keresőmező figyelése
        setupSearchView();

        // Előzmény törlés gomb bekötése
        binding.btnClearHistory.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.clearHistory();
                Toast.makeText(requireContext(), "Keresési előzmények törölve", Toast.LENGTH_SHORT).show();
            }
        });

        // a szűrők beállítása
        setupFilterChips();
    }

    private void observeViewModel() {
        // Találatok figyelése
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), items -> {
            if (items != null && binding.searchView.getQuery().length() >= 3) {
                contentAdapter.submitList(items);
            }
        });

        // Előzmények figyelése
        viewModel.getRecentHistory().observe(getViewLifecycleOwner(), history -> {
            if (binding.searchView.getQuery().length() < 3) {
                displayHistory(history);
                boolean hasHistory = history != null && !history.isEmpty();
                binding.btnClearHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
                binding.textHistoryLabel.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
                binding.recyclerView.setAlpha(hasHistory ? 0.6f : 1.0f);
            }
        });

        // hiba esetén figyelmeztetés toas-on keresztül
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Ez biztosítja, hogy a mentés ikonok mindig helyesek legyenek megjelenítve, állapottól függően
        viewModel.getAllSaved().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null) {
                // Átadjuk a friss listát az adapternek
                contentAdapter.setSavedItems(favorites);
            }
        });
    }

    // előzmény listázása
    private void displayHistory(List<SearchHistory> history) {
        if (history != null && !history.isEmpty()) {
            List<MediaItem> historyItems = new ArrayList<>();
            for (SearchHistory h : history) {
                historyItems.add(h.toMediaItem());
            }
            contentAdapter.submitList(historyItems);
        } else {
            contentAdapter.submitList(new ArrayList<>());
        }
    }

    // keresés előtti ellenőrzések, majd a keresés indítása
    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Töröljük az előző, még nem futtatott keresést
                if(searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                // a felesleges üres karakterek levágása
                String cleanText = newText.trim();

                // 3 karakter után indul csak a keresés
                if (cleanText.length() >= 3) {
                    binding.btnClearHistory.setVisibility(View.GONE);
                    binding.textHistoryLabel.setVisibility(View.GONE);
                    binding.recyclerView.setAlpha(1.0f);
                    // Késleltetett indítás (500ms), hogy ne terheljük az API-t minden betűnél
                    searchRunnable = () -> viewModel.performSearch(newText);
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    // Ha nincs már szöveg a mezőben, akkor az előzményeket mutatjuk
                    List<SearchHistory> history = viewModel.getRecentHistory().getValue();
                    displayHistory(history);
                    boolean hasHistory = history != null && !history.isEmpty();
                    binding.btnClearHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
                    binding.textHistoryLabel.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
                    binding.recyclerView.setAlpha(hasHistory ? 0.8f : 1.0f);
                }
                return true;
            }

            // keresés véglegesítése
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                String cleanQuery = query.trim();
                if (query.length() >= 3) {
                    viewModel.performSearch(query);
                }
                // Billentyűzet elrejtése
                binding.searchView.clearFocus();
                return true;
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView, ContentAdapter adapter) {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(adapter);
    }

    // --- kattintás kezelés (ContentClickListener implementáció) ---
    @Override
    public void onItemClick(MediaItem item) {
        // Mentés az előzmények közé
        viewModel.addToHistory(item);

        // Részletek activity megnyitása kattintásra
        Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    private void setupFilterChips() {
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            SearchViewModel.FilterType selectedFilter = SearchViewModel.FilterType.ALL;

            // ha ki van választva valamelyik szűrési paraméter, akkor megmondjuk, hogy melyik az
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);

                if (id == R.id.newContentChipButton) {
                    selectedFilter = SearchViewModel.FilterType.NEW;
                } else if (id == R.id.bestContentChipButton) {
                    selectedFilter = SearchViewModel.FilterType.POPULAR;
                } else if (id == R.id.movieChipButton) {
                    selectedFilter = SearchViewModel.FilterType.MOVIES;
                } else if (id == R.id.seriesChipButton) {
                    selectedFilter = SearchViewModel.FilterType.SERIES;
                }
            }

            if (viewModel != null) {
                viewModel.setFilter(selectedFilter);
            }
        });
    }

    // adott tétel mentése saját listába
    @Override
    public void onBookmarkClick(MediaItem item, boolean isCurrentlySaved) {
        // állapot mentése (később DB)
        viewModel.toggleFavoriteStatus(item, isCurrentlySaved);

        // Visszajelzés
        if (isCurrentlySaved) {
            Toast.makeText(requireContext(), "Eltávolítva", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Mentve", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}