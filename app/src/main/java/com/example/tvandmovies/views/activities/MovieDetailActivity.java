package com.example.tvandmovies.views.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.databinding.ActivityMovieDetailBinding;
import com.example.tvandmovies.R;
import com.example.tvandmovies.model.Movie;

import java.text.SimpleDateFormat;
import java.util.Locale;

import eightbitlab.com.blurview.RenderScriptBlur;

public class MovieDetailActivity extends AppCompatActivity {

    private ActivityMovieDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovieDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
    }

    private void setVariable() {
        Movie item = (Movie) getIntent().getSerializableExtra("object");
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new GranularRoundedCorners(0,0,50,50));

        Glide.with(this)
                .load(item.getFullPosterUrl())
                .apply(requestOptions)
                .into(binding.MoviePoster);

        binding.movieDetailTitle.setText(item.getTitle());

        // a string és double összefűzés miatt van erre a megoldásra szükség, a lokalizálható mód miatt
        binding.imdbText.setText(binding.getRoot().getContext().getString(R.string.imdb_rating, item.getVote_avg()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM", Locale.getDefault());
        try {
            if (item.getReDate() != null) {
                binding.movieDateAndTime.setText(sdf.format(item.getReDate())+ " - ");
            } else {
                binding.movieDateAndTime.setText("N/A");
            }
        } catch (Exception e) {
            binding.movieDateAndTime.setText("Invalid date");
        }

        binding.summaryDescription.setText(item.getDescription());

        binding.closeButton.setOnClickListener(v -> finish());

        float radius = 10f;
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowsBackground = decorView.getBackground();

        binding.blurView.setupWith(rootView, new RenderScriptBlur(this))
                .setFrameClearDrawable(windowsBackground)
                .setBlurRadius(radius);
        binding.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        binding.blurView.setClipToOutline(true);
    }
}