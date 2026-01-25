package com.example.tvandmovies.UI.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.databinding.ActivityContentDetailBinding;
import com.example.tvandmovies.R;
import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.utilities.FullScreenMode;

import java.text.SimpleDateFormat;
import java.util.Locale;

import eightbitlab.com.blurview.RenderScriptBlur;

public class ActivityContentDetail extends AppCompatActivity {
    private ActivityContentDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setVariable();

        // teljes kijelzős mód
        FullScreenMode.setupWindowFlags(this);
    }

    // Értékek mezőkhöz rendelése
    private void setVariable() {
        MediaItem item = (MediaItem) getIntent().getSerializableExtra("object");

        // a poszer betöltése lekerekített alsó sarkokkal
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new GranularRoundedCorners(0,0,50,50));

        Glide.with(this)
                .load(item.getFullPosterUrl())
                .apply(requestOptions)
                .into(binding.MediaPoster);

        // a szükséges változók beállítása
        binding.mediaDetailTitle.setText(item.getTitle());

        // a string és double összefűzés miatt van erre a megoldásra szükség, a lokalizálható mód miatt
        binding.imdbText.setText(binding.getRoot().getContext().getString(R.string.imdb_rating, item.getVote_avg()));

        // a megfelelő dátum megjelnítés érdekében formázni kell az API-tól kapott értéket
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd", Locale.getDefault());
        try {
            if (item.getReDate() != null) {
                binding.mediaDateAndPlayTime.setText(sdf.format(item.getReDate()));
            } else {
                binding.mediaDateAndPlayTime.setText("N/A");
            }
        } catch (Exception e) {
            binding.mediaDateAndPlayTime.setText("Helytelen dátum formátum");
        }

        binding.summaryDescription.setText(item.getDescription());

        // a close button megnyomására bezárja a nézetet -> visszatér az activityMain-be
        binding.closeButton.setOnClickListener(v -> finish());

        // a blur hatás beállítása, finomítás
        float radius = 10f;

        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowsBackground = decorView.getBackground();

        binding.blurView.setupWith(rootView, new RenderScriptBlur(this))
                .setFrameClearDrawable(windowsBackground)
                .setBlurRadius(radius);
        binding.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        binding.blurView.setClipToOutline(true); // ne lógjon túl a kijelölt területen
    }
}