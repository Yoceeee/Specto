package com.example.tvandmovies.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper; // Fontos a Handlerhez
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.controllers.SearchController;
import com.example.tvandmovies.databinding.FragmentSearchBinding;
import com.example.tvandmovies.interfaces.SearchViewInterface;
import com.example.tvandmovies.model.Movie;
import com.example.tvandmovies.views.activities.MovieDetailActivity; // A Detail marad Activity!
import com.example.tvandmovies.views.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchViewInterface {
    private FragmentSearchBinding binding;
    private SearchController searchController;
    private MovieAdapter movieAdapter;
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
        super.onCreate(savedInstanceState);

        movieAdapter = new MovieAdapter(new ArrayList<>(), true);
        setupRecyclerView(binding.recyclerView, movieAdapter);
        searchController = new SearchController(this);

        // keresőmező figyelése, aktív keresési mód
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // csak akkor induljon a keresés, ha nem üres és nagyobb, mint 3 karaktert adott meg a user
            @Override
            public boolean onQueryTextChange(String newText) {
                if(searchRunnable != null){
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (newText.length() >= 3){
                    searchRunnable = () -> performSearch(newText);
                    searchHandler.postDelayed(searchRunnable, 500); // 500 ms-es várakozás
                }
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    //trigerreli a controllerben lévő API hívást minden query changenél
    private void performSearch(String query) {
        if (searchController != null){
            searchController.searchMoviesAndSeries(query);
        }
    }

    // a recyclerView beállítása, horizontális nézetbe
    private void setupRecyclerView(RecyclerView recyclerView, MovieAdapter adapter) {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(adapter);
    }

    public void updateSearchResults(List<Movie> movies) {
        movieAdapter.setMovieList(movies);
    }

    // toast message küldése hiba esetén
    public void showError(String message){
        if (getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Memory leak ellen
    }
}