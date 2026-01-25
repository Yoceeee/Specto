package com.example.tvandmovies.UI.explore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ÚJ IMPORT
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.FragmentSearchBinding;
import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.UI.activities.ActivityContentDetail;
import com.example.tvandmovies.UI.adapter.ContentAdapter;

import java.util.ArrayList;

// 1. Interface csere: SearchViewInterface HELYETT ContentClickListener
public class SearchFragment extends Fragment implements ContentAdapter.ContentClickListener {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel; // Controller helyett ViewModel
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
        contentAdapter = new ContentAdapter(this, true);
        setupRecyclerView(binding.recyclerView, contentAdapter);

        // ViewModel példányosítása
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Feliratkozás az adatokra (Observer)
        observeViewModel();

        // Keresőmező figyelése
        setupSearchView();

        // a szűrők beállítása
        setupFilterChips();
    }

    private void observeViewModel() {
        // Találatok figyelése
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                contentAdapter.submitList(items);
                // Ha üres a lista, itt lehetne "Nincs találat" szöveget megjeleníteni
            }
        });

        // Hiba figyelése
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Töröljük az előző, még nem futtatott keresést
                if(searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                // 3 karakter után indul csak a keresés
                if (newText.length() >= 3) {
                    // Késleltetett indítás (500ms), hogy ne terheljük az API-t minden betűnél
                    searchRunnable = () -> viewModel.performSearch(newText);
                    searchHandler.postDelayed(searchRunnable, 500);
                } else if (newText.isBlank() || newText.trim().isEmpty()) {
                    // Ha kitörölte a szöveget, ürítjük a listát
                    contentAdapter.submitList(new ArrayList<>());
                    //viewModel.setQ(""); // Fontos: ViewModel érték nullázása!
                    viewModel.performSearch(""); // Opcionális: üres lista vagy alapállapot betöltése
                }
                return true;
            }

            // keresés véglegesítése
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
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
        // Részletek activity megnyitása kattintásra
        Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    private void setupFilterChips() {
        // Figyeld meg: setOnCheckedStateChangeListener (State szó benne van!)
        // A második paraméter itt már egy lista (checkedIds), nem egy int.
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {

            SearchViewModel.FilterType selectedFilter = SearchViewModel.FilterType.ALL;

            // Mivel singleSelection="true", a lista vagy üres, vagy 1 eleme van.
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0); // Kivesszük az első (egyetlen) ID-t

                if (id == R.id.newContentChipButton) {
                    selectedFilter = SearchViewModel.FilterType.NEW;
                } else if (id == R.id.bestContentChipButton) {
                    selectedFilter = SearchViewModel.FilterType.SERIES;
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
    public void onBookmarkClick(MediaItem item) {
        // Mentés logika (később DB)
        Toast.makeText(requireContext(), "Mentve: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}