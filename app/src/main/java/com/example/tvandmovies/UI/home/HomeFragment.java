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

import com.bumptech.glide.Glide;
import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.activities.ActivityContentDetail;
import com.example.tvandmovies.UI.activities.SeeAllActivity;
import com.example.tvandmovies.databinding.FragmentHomeBinding;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.utilities.SharedViewModel;

public class HomeFragment extends Fragment implements ContentAdapter.ContentClickListener{
    private FragmentHomeBinding binding;

    private SharedViewModel sharedViewModel;

    private HomeViewModel viewModel;
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

        // ViewModel inicializálása (Fontos: a requireActivity()-hez kötjük, így globális lesz a Fragmentek között!)
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            binding.userName.setText(username != null ? username : "Felhasználó");
        });

        // username lekérése, ha szükséges
        view.post(() -> {
            sharedViewModel.loadUsernameIfNeeded();
        });

        // Adapterek és RecyclerView inicializálása
        popContentAdapter = new ContentAdapter(this, 0);
        newContentAdapter = new ContentAdapter(this, 0);
        allTimeBestAdapter = new ContentAdapter(this, 0);

        initRecyclerView(binding.recyclerViewPopularMovie, popContentAdapter);
        initRecyclerView(binding.recyclerViewNewMovie, newContentAdapter);
        initRecyclerView(binding.recyclerViewAllTimeBestMovie, allTimeBestAdapter);

        // Ez köti össze a Fragmentet a memóriában élő ViewModellel.
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // FELIRATKOZÁS AZ ADATOKRA (OBSERVE)
        subscribeToViewModel();

        // ÁLLAPOT VISSZATÖLTÉS
        if (savedInstanceState != null) {
            currentSelectedId = savedInstanceState.getInt(KEY_SELECTED_ID, R.id.btnMovies);
        }

        // GOMB FIGYELŐ a sorozat / filmek váltáshoz (LISTENER)
        binding.radioGroupToggle.setOnCheckedChangeListener((group, checkedId) -> {
            currentSelectedId = checkedId;
            if (checkedId == R.id.btnMovies) {
                setupUIMovies(); // Szövegek átírása
                viewModel.setContentType("movies"); // megfelelő kategória beállítása
            } else if (checkedId == R.id.btnSeries) {
                setupUISeries();
                viewModel.setContentType("series");
            }
        });

        viewModel.getAllSaved().observe(getViewLifecycleOwner(), saved ->{
            if(saved != null){
                popContentAdapter.setSavedItems(saved);
                newContentAdapter.setSavedItems(saved);
                allTimeBestAdapter.setSavedItems(saved);
            }
        });

        // Lehúzás mozdulat (frissítés) érzékelése
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // Szólunk a ViewModelnek, hogy töltsön újra mindent
            viewModel.refreshData();

            // Ha a letöltés elindult, eltüntethetjük a frissítés ikonját
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        // az összes elem megtekintése gombra kattintás (Népszerű)
        binding.btnSeeAllPopular.setOnClickListener(v -> {
            boolean isMovie = (currentSelectedId == R.id.btnMovies);
            openSeeAllActivity(
                    isMovie ? "POPULAR_MOVIES" : "POPULAR_SERIES",
                    isMovie ? "Népszerű filmek" : "Népszerű sorozatok"
            );
        });

        // az összes elem megtekintése gombra kattintás (Népszerű)
        binding.btnSeeAllNew.setOnClickListener(v -> {
            boolean isMovie = (currentSelectedId == R.id.btnMovies);
            openSeeAllActivity(
                    isMovie ? "NEW_MOVIES" : "NEW_SERIES",
                    isMovie ? "Új filmek" : "Új sorozatok"
            );
        });

        binding.btnSeeAllBest.setOnClickListener(v -> {
            openSeeAllActivity("TOP_RATED_MOVIES", "Legjobb filmek");
        });

        binding.radioGroupToggle.check(currentSelectedId);
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

    // content mentése saját listára listener
    @Override
    public void onBookmarkClick(MediaItem item, boolean isCurrentlySaved){
       viewModel.toggleSavedStatus(item, isCurrentlySaved);

       if (isCurrentlySaved){
         Toast.makeText(requireContext(), "Eltávolítva a mentett listából", Toast.LENGTH_SHORT).show();
       } else {
           Toast.makeText(requireContext(), "Mentve a kedvencek közé", Toast.LENGTH_SHORT).show();
       }
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

        // Külön figyeljük a Hero állapotot
        viewModel.getHeroState().observe(getViewLifecycleOwner(), state -> {
            if (state != null) {
                setupHeroHeader(state);
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
        binding.btnSeeAllBest.setVisibility(View.VISIBLE);

        binding.allTimeBestTitle.setText("Minden idők legjobb");
        binding.allTimeBestText.setText("filmjei");

        // Kényszerítjük a szülő konténert, hogy számolja újra a magasságot, hogy ne vágja le az utolsó listát
        binding.getRoot().post(() -> {
            binding.getRoot().requestLayout();
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
        binding.btnSeeAllBest.setVisibility(View.GONE);
        binding.recyclerViewAllTimeBestMovie.setVisibility(View.GONE);
    }

    // megjeleníti a töltő karikát
    private void setLoading(boolean isLoading) {
        int visibility = isLoading ? View.VISIBLE : View.GONE;
        binding.popProgressBar.setVisibility(visibility);
        binding.newProgressBar.setVisibility(visibility);
        binding.allTimeBestProgressBar.setVisibility(visibility);
    }

    // --- HERO HEADER BEÁLLÍTÁSA ---
    private void setupHeroHeader(HomeViewModel.HeroUiState state) {
        binding.heroContainer.setVisibility(View.VISIBLE);

        // Nincs logika, csak set...
        binding.heroTitle.setText(state.title);
        binding.heroTags.setText(state.genreText); // Kész szöveget kapunk!

        Glide.with(this)
                .load(state.imageUrl) // Kész URL-t kapunk!
                .thumbnail(Glide.with(this).load(state.imageUrl).sizeMultiplier(0.2f))
                .placeholder(R.drawable.gradient_transparent_to_color)
                .centerCrop()
                .into(binding.heroImage);

        // Klikk események
        binding.heroContainer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
            intent.putExtra("object", state.originalItem);
            startActivity(intent);
        });
    }

    // beállítja a megfelelő grides nézetet az "összes" nézetre
    private void openSeeAllActivity(String categoryType, String title) {
        Intent intent = new Intent(requireContext(), SeeAllActivity.class);
        intent.putExtra("CATEGORY_TYPE", categoryType);
        intent.putExtra("CATEGORY_TITLE", title);
        startActivity(intent);
    }


    // a kártyák inicializálása
    private void initRecyclerView(RecyclerView recyclerView, ContentAdapter adapter){
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        //recyclerView.setItemViewCacheSize(10); // 10 db kártyát tárol így a memóriában
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