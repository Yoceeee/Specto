package com.example.tvandmovies.UI.home;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.activities.ActivityContentDetail;
import com.example.tvandmovies.databinding.FragmentHomeBinding;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.model.MediaItem;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements ContentAdapter.ContentClickListener{
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel; // Controller helyett ViewModel
    private ContentAdapter popContentAdapter;
    private ContentAdapter newContentAdapter;
    private ContentAdapter allTimeBestAdapter;

    // Állapotmentéshez a kiválasztott gomb ID-ja
    private int currentSelectedId = R.id.btnMovies;
    private static final String KEY_SELECTED_ID = "selected_id_key";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstance){
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adapterek és RecyclerView inicializálása
        popContentAdapter = new ContentAdapter(this, false);
        newContentAdapter = new ContentAdapter(this, false);
        allTimeBestAdapter = new ContentAdapter(this, false);

        initRecyclerView(binding.recyclerViewPopularMovie, popContentAdapter);
        initRecyclerView(binding.recyclerViewNewMovie, newContentAdapter);
        initRecyclerView(binding.recyclerViewAllTimeBestMovie, allTimeBestAdapter);

        // Ez a sor köti össze a Fragmentet a memóriában élő ViewModellel.
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // FELIRATKOZÁS AZ ADATOKRA (OBSERVE)
        subscribeToViewModel();

        // ÁLLAPOT VISSZATÖLTÉS (Ha elforgattad a telefont)
        if (savedInstanceState != null) {
            currentSelectedId = savedInstanceState.getInt(KEY_SELECTED_ID, R.id.btnMovies);
        }

        // GOMB FIGYELŐ a sorozat / filmek váltáshoz (LISTENER)
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                currentSelectedId = checkedId; // Elmentjük, mi van kiválasztva

                if (checkedId == R.id.btnMovies) {
                    setupUIMovies(); // Szövegek átírása
                    viewModel.setContentType("movies"); // Adat kérése
                } else if (checkedId == R.id.btnSeries) {
                    setupUISeries(); // Szövegek átírása
                    viewModel.setContentType("series"); // Adat kérése
                }
            }
        });

        binding.toggleGroup.check(currentSelectedId);
    }

    // ContentClickListener által észlelt események kezelése
    // a kártyákra kattintás esetén indítja az activity-t
    @Override
    public void onItemClick(MediaItem item) {
        // Részletek megnyitása
        Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(MediaItem item){
        // amikor a kártyán lévő mentésre kattintunk -> TODO: viewModel.saveItem (majd)
        Toast.makeText(requireContext(), "Mentve" + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Itt figyeljük a ViewModel változásait.
     * Amint adat érkezik a háttérből, ezek a blokkok lefutnak.
     */
    private void subscribeToViewModel() {
        // Népszerű lista figyelése
        viewModel.getPopularContent().observe(getViewLifecycleOwner(), content -> {
            if (content != null) {
                popContentAdapter.submitList(content);
            }
        });

        // Új lista figyelése
        viewModel.getNewContent().observe(getViewLifecycleOwner(), content -> {
            if (content != null) {
                newContentAdapter.submitList(content);
            }
        });

        // minden idők legjobbjai lista figyelése
        viewModel.getAllTimeBestContent().observe(getViewLifecycleOwner(), content ->{
            if (content != null) {
                allTimeBestAdapter.submitList(content);
            }
        });

        // Töltési állapot figyelése
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            setLoading(isLoading);
        });

        // Hibaüzenetek figyelése
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                setLoading(false); // Hiba esetén levesszük a homokórát
            }
        });
    }


    // --- UI SEGÉDFÜGGVÉNYEK ---
    private void setupUIMovies() {
        binding.popTitle.setText("Népszerű");
        binding.popularText.setText("filmek");
        binding.newTitle.setText("Új");
        binding.newText.setText("filmek");

        // All Time Best szekció megjelenítése (VISIBLE)
        binding.allTimeBestTitle.setVisibility(View.VISIBLE);
        binding.allTimeBestText.setVisibility(View.VISIBLE);
        binding.recyclerViewAllTimeBestMovie.setVisibility(View.VISIBLE);

        binding.allTimeBestTitle.setText("Minden idők legjobb");
        binding.allTimeBestText.setText("filmjei");

        // a recView kényszerített újrarajzolása
        binding.recyclerViewAllTimeBestMovie.post(() ->{
            binding.recyclerViewAllTimeBestMovie.requestLayout();
            binding.recyclerViewAllTimeBestMovie.invalidate();
        });
    }

    private void setupUISeries() {
        binding.popTitle.setText("Népszerű");
        binding.popularText.setText("sorozatok");
        binding.newTitle.setText("Új");
        binding.newText.setText("epizódok a mai napon");

        // All Time Best szekció elrejtése (GONE)
        // Mivel sorozatoknál nincs ilyen lista (vagy nem töltjük be), el kell tüntetni. // TODO: ha lesz ilyen lista, akkor ezt meg kell változtatni
        binding.allTimeBestTitle.setVisibility(View.GONE);
        binding.allTimeBestText.setVisibility(View.GONE);
        binding.recyclerViewAllTimeBestMovie.setVisibility(View.GONE);
    }

    // megjeleníti a töltő karikát
    private void setLoading(boolean isLoading) {
        int visibility = isLoading ? View.VISIBLE : View.GONE;
        binding.popProgressBar.setVisibility(visibility);
        binding.newProgressBar.setVisibility(visibility);
        binding.allTimeBestProgressBar.setVisibility(visibility);
    }

    // a kártyák inicializálása
    private void initRecyclerView(RecyclerView recyclerView, ContentAdapter adapter){
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerView.setItemViewCacheSize(40); // 40 db kártyát tárol így a memóriában
        recyclerView.setHasFixedSize(true); // jelzés, hogy a kártyák mérete nem változik
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_ALWAYS);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_ID, currentSelectedId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}