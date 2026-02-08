package com.example.tvandmovies.UI.saved;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tvandmovies.UI.activities.ActivityContentDetail;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.databinding.FragmentBookmarkBinding;
import com.example.tvandmovies.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment implements ContentAdapter.ContentClickListener {
    private FragmentBookmarkBinding binding;
    private BookmarkViewModel viewModel;
    private ContentAdapter movieAdapter;
    private ContentAdapter seriesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adapterek beállítása (false a flag, hogy ne Grid legyen, hanem lista)
        movieAdapter = new ContentAdapter(this, false);
        seriesAdapter = new ContentAdapter(this, false);

        // RecyclerView-k beállítása vizszintesre
        binding.rvMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvMovies.setAdapter(movieAdapter);

        binding.rvSeries.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvSeries.setAdapter(seriesAdapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);

        // Adatok figyelése
        viewModel.getFavorites().observe(getViewLifecycleOwner(), allFavorites -> {
            if (allFavorites != null) {
                // Szinkronizálás az ikonokhoz
                movieAdapter.setSavedItems(allFavorites);
                seriesAdapter.setSavedItems(allFavorites);

                // Szűrés (Java Stream API vagy sima loop)
                List<MediaItem> movies = new ArrayList<>();
                List<MediaItem> series = new ArrayList<>();

                for (MediaItem item : allFavorites) {
                    if ("movie".equals(item.getMediaType())) {
                        movies.add(item);
                    } else {
                        series.add(item);
                    }
                }

                // Listák átadása
                movieAdapter.submitList(movies);
                seriesAdapter.submitList(series);

                // Üres állapotok kezelése (visibility toggle)
                binding.tvEmptyMovies.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
                binding.tvEmptySeries.setVisibility(series.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        // TODO: részletes/teljes nézet gombok kezelése
        //binding.btnOpenMoviesGrid.setOnClickListener(v -> openFullGrid("movie"));
        //binding.btnOpenSeriesGrid.setOnClickListener(v -> openFullGrid("tv"));
    }

    @Override
    public void onItemClick(MediaItem item) {
        // Ugyanaz a logika: Részletek megnyitása
        Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(MediaItem item, boolean isCurrentlySaved) {
        viewModel.removeFromFavorites(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
