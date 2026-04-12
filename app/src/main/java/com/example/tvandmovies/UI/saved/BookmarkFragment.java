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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.activities.ActivityContentDetail;
import com.example.tvandmovies.UI.activities.SeeAllActivity;
import com.example.tvandmovies.UI.adapter.ContinueWatchingAdapter;
import com.example.tvandmovies.UI.adapter.SavedContentAdapter;
import com.example.tvandmovies.databinding.FragmentBookmarkBinding;
import com.example.tvandmovies.model.entities.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment {
    private FragmentBookmarkBinding binding;
    private BookmarkViewModel viewModel;
    private SavedContentAdapter movieAdapter;
    private SavedContentAdapter seriesAdapter;
    private ContinueWatchingAdapter continueWatchingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(BookmarkViewModel.class);

        // a két különböző típusú kattintás kezelése
        SavedContentAdapter.OnItemClickListener clickListener = new SavedContentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                openDetailActivity(item);
            }

            @Override
            public void onItemLongClick(MediaItem item) {
                com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
                        new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog);

                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_saved_action, null);
                bottomSheet.setContentView(sheetView);

                // Beállítjuk a szöveget a film címére
                android.widget.TextView title = sheetView.findViewById(R.id.bsTitle);
                title.setText(item.getTitle());

                //  Mi történjen, ha rányom a piros törlés gombra
                com.google.android.material.button.MaterialButton btnDelete = sheetView.findViewById(R.id.bsBtnDelete);
                btnDelete.setOnClickListener(v -> {
                    viewModel.removeFromFavorites(item);
                    Toast.makeText(getContext(), "Törölve", Toast.LENGTH_SHORT).show();
                    bottomSheet.dismiss(); // Eltüntetjük a panelt
                });

                // Megjelenítjük
                bottomSheet.show();
            }
        };

        // A kibővített Listener a folyamatban lévő mentett sorozatoknak
        ContinueWatchingAdapter.OnContinueWatchingListener continueWatchingListener = new ContinueWatchingAdapter.OnContinueWatchingListener() {
            @Override
            public void onItemClick(MediaItem item) {
                openDetailActivity(item);
            }
        };

        // Adapterek beállítása
        continueWatchingAdapter = new ContinueWatchingAdapter(continueWatchingListener);
        movieAdapter = new SavedContentAdapter(clickListener);
        seriesAdapter = new SavedContentAdapter(clickListener);

        // Folyamatban lévő sorozatok
        binding.rvContinueWatching.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvContinueWatching.setAdapter(continueWatchingAdapter);

        // RecyclerView-k beállítása vizszintesre
        binding.savedMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.savedMovies.setAdapter(movieAdapter);

        binding.savedSeries.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.savedSeries.setAdapter(seriesAdapter);

        // a mentett tételeket vizsgálja, hogy melyik tétel film / sorozat, majd a megfelelő adapternek átadja
        viewModel.getFavorites().observe(getViewLifecycleOwner(), allFavorites -> {
            if (allFavorites != null) {
                // Szinkronizálás az ikonokhoz
                List<MediaItem> movies = new ArrayList<>();
                List<MediaItem> series = new ArrayList<>();

                for (MediaItem item : allFavorites) {
                    if ("movie".equals(item.getMediaType())) {
                        movies.add(item);
                    } else {
                        series.add(item);
                    }
                }

                // Tételek átadása az ÚJ adapternek
                movieAdapter.setSavedItems(movies);
                seriesAdapter.setSavedItems(series);

                //---- Üres állapotok kezelése ----
                binding.tvEmptyMovies.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
                binding.tvEmptySeries.setVisibility(series.isEmpty() ? View.VISIBLE : View.GONE);

                binding.btnOpenMoviesGrid.setVisibility(movies.size() > 3 ? View.VISIBLE : View.GONE);
                binding.btnOpenSeriesGrid.setVisibility(series.size() > 3 ? View.VISIBLE : View.GONE);
            }
        });

        // folyamatban lévő sorozatok figyelése
        viewModel.getContinueWatching().observe(getViewLifecycleOwner(), displayList -> {
            if (displayList != null) {
                continueWatchingAdapter.setSeries(displayList);

                // UI elemek elrejtése/megjelenítése
                int visibility = displayList.isEmpty() ? View.GONE : View.VISIBLE;
                binding.rvContinueWatching.setVisibility(visibility);
                binding.tvContinueWatchingTitle.setVisibility(visibility);
            }
        });

        // Mentett Filmek "Összes" gombja
        binding.btnOpenMoviesGrid.setOnClickListener(v -> {
            openSeeAllActivity("SAVED_MOVIES", "Mentett filmek");
        });

        // Mentett Sorozatok "Összes" gombja
        binding.btnOpenSeriesGrid.setOnClickListener(v -> {
            openSeeAllActivity("SAVED_SERIES", "Mentett sorozatok");
        });
    }

    // Részletek nézet megnyitása
    private void openDetailActivity(MediaItem item) {
        Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    // "Összes" nézet (rácsos) megnyitása
    private void openSeeAllActivity(String categoryType, String title) {
        Intent intent = new Intent(requireContext(), SeeAllActivity.class);
        intent.putExtra("CATEGORY_TYPE", categoryType);
        intent.putExtra("CATEGORY_TITLE", title);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Amikor a user visszanavigál, kikényszerítjük a frissítést
        if (viewModel != null) {
            viewModel.forceRefreshEpisodes();
        }
    }
}
