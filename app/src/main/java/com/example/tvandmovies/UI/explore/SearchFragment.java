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

import com.example.tvandmovies.databinding.FragmentSearchBinding;
import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.UI.activities.MovieDetailActivity;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.UI.explore.SearchViewModel; // ÚJ IMPORT

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

        // 2. Adapter inicializálás: átadjuk a 'this'-t mint listenert
        contentAdapter = new ContentAdapter(this, true);
        setupRecyclerView(binding.recyclerView, contentAdapter);

        // 3. ViewModel példányosítása
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // 4. Feliratkozás az adatokra (Observer)
        observeViewModel();

        // 5. Keresőmező figyelése
        setupSearchView();
    }

    private void observeViewModel() {
        // Találatok figyelése
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                contentAdapter.setContentList(items);
                // Ha üres a lista, itt lehetne "Nincs találat" szöveget megjeleníteni
            }
        });

        // Hiba figyelése
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Töltés figyelése (opcionális)
        /*
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
             binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        */
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Töröljük az előző, még nem futtatott keresést
                if(searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                if (newText.length() >= 3) {
                    // Késleltetett indítás (500ms), hogy ne terheljük az API-t minden betűnél
                    searchRunnable = () -> viewModel.performSearch(newText);
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    // Ha kitörölte a szöveget, ürítjük a listát
                    contentAdapter.setContentList(new ArrayList<>());
                }
                return true;
            }

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

    // --- KATTINTÁS KEZELÉS (ContentClickListener implementáció) ---

    @Override
    public void onItemClick(MediaItem item) {
        // Részletek megnyitása
        Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

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