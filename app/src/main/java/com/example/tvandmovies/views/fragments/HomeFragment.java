package com.example.tvandmovies.views.fragments;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;
import com.example.tvandmovies.controllers.MovieController;
import com.example.tvandmovies.databinding.FragmentHomeBinding;
import com.example.tvandmovies.interfaces.HomeViewInterface;
import com.example.tvandmovies.model.Movie;
import com.example.tvandmovies.views.adapter.MovieAdapter;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements HomeViewInterface {
    private FragmentHomeBinding binding;
    private MovieController movieController;
    private MovieAdapter popularMovieAdapter;
    private MovieAdapter newMovieAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstance){
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // a tötltés jelző beállítása láthatóra
        setLoading(true);

        // a keresősávra kattintva átnavigál a kereső nézetre
        binding.movieSearchBar.setFocusable(false);
        binding.movieSearchBar.setOnClickListener(v -> {
            if (getActivity() != null){
                ChipNavigationBar navBar = getActivity().findViewById(R.id.bottom_nav_bar);
                if (navBar != null){
                    // ekkor váltunk explore (keresés) fülre
                    navBar.setItemSelected(R.id.explore, true);
                }
            }
        });

        // adapterek és recyclerView inicializálása
        popularMovieAdapter = new MovieAdapter(new ArrayList<>(), false);
        newMovieAdapter = new MovieAdapter(new ArrayList<>(), false);

        initRecyclerView(binding.recyclerViewPopularMovie, popularMovieAdapter);
        initRecyclerView(binding.recyclerViewNewMovie, newMovieAdapter);

        // Filmek betöltése
        movieController = new MovieController(this);
        movieController.loadMovies();
    }

    // a töltést jelző bar egyszerűbb használata
    private void setLoading(boolean isLoading) {
        binding.popProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.newProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    // A recyclerView-t itt inicializálom, mert több helyen kelleni fog
    private void initRecyclerView(RecyclerView recyclerView, MovieAdapter adapter){
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        // a túlpörgetés miatt bekövetkező hullámzást tiltja
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(adapter);
    }

    // Népszerű filmek beállítása az adapter segítségével
    @Override
    public void updatePopularMovieList(List<Movie> movies) {
        popularMovieAdapter.setMovieList(movies);
        setLoading(false); // amint megjelennek a filmek eltünik a progbar
    }

    // Új filmek beállítása az adapter segítségével
    @Override
    public void updateNewMovies(List<Movie> movies){
        newMovieAdapter.setMovieList(movies);
        setLoading(false);
    }

    // toast message küldése hiba esetén
    @Override
    public void showError(String message) {
        if (getContext() != null){
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    // Fontos a memória felszabadításához, amikor nincs már használva a fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
