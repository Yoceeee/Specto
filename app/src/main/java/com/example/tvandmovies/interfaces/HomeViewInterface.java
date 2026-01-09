package com.example.tvandmovies.interfaces;

import java.util.List;
import com.example.tvandmovies.model.Movie;

public interface HomeViewInterface {
    void updatePopularMovieList(List<Movie> movies);
    void updateNewMovies(List<Movie> movies);
    void showError(String message);
}
