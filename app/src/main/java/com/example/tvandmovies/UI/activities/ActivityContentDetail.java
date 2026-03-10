package com.example.tvandmovies.UI.activities;

import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.saved.DetailViewModel;
import com.example.tvandmovies.databinding.ActivityContentDetailBinding;
import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.utilities.FullScreenMode;
import com.example.tvandmovies.utilities.GenreHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityContentDetail extends AppCompatActivity {

    private ActivityContentDetailBinding binding;
    private DetailViewModel viewModel;
    private boolean isBookmarked = false;
    private MediaItem currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Teljes képernyős mód
        FullScreenMode.setupWindowFlags(this);

        // ViewModel inicializálás
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        // Adatok betöltése
        loadData();
    }

    private void loadData() {
        // Adatok kinyerése az Intentből
        if (getIntent().hasExtra("object")) {
            currentItem = (MediaItem) getIntent().getSerializableExtra("object");
        }

        if (currentItem == null) {
            Toast.makeText(this, "Hiba az adatok betöltésekor", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kép betöltése (Parallax header)
        // Itt már nem kell lekerekítés, mert a kép kitölti a fejlécet
        Glide.with(this)
                .load(currentItem.getPosterDetailUrl())
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(binding.MediaPoster);

        // Szöveges adatok
        binding.mediaDetailTitle.setText(currentItem.getTitle());
        binding.summaryDescription.setText(currentItem.getDescription());
        binding.imdbText.setText(currentItem.getFormatedRating());

        // Dátum formázása
        formatDate(currentItem);

        // Műfajok beállítása (RecyclerView)
        setupGenreList(currentItem.getGenreIds());

        // Mentés gomb (FAB) logika
        setupBookmarkButton();
    }

    private void formatDate(MediaItem item) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());
        try {
            if (item.getReDate() != null) {
                binding.mediaDateAndPlayTime.setText(sdf.format(item.getReDate()));
            } else {
                binding.mediaDateAndPlayTime.setText("N/A");
            }
        } catch (Exception e) {
            binding.mediaDateAndPlayTime.setText("-");
        }
    }

    private void setupGenreList(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return;

        // Átalakítjuk az ID-kat nevekké
        List<String> genreNames = new ArrayList<>();
        for (Integer id : genreIds) {
            String name = GenreHelper.getGenreName(id);
            if (!name.isEmpty()) {
                genreNames.add(name);
            }
        }

        // RecyclerView beállítása
        binding.mediaGenre.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.mediaGenre.setAdapter(new GenreAdapter(genreNames));
    }

    private void setupBookmarkButton() {
        // Figyeljük az adatbázist
        viewModel.getSavedById(currentItem.getId()).observe(this, dbItem -> {
            if (dbItem != null) {
                isBookmarked = true;
                binding.saveMedia.setImageResource(R.drawable.bookmark_filled);
            } else {
                isBookmarked = false;
                binding.saveMedia.setImageResource(R.drawable.bookmark_save); // Vagy sima bookmark
            }
        });

        // Kattintás kezelése
        binding.saveMedia.setOnClickListener(v -> {
            if (isBookmarked) {
                viewModel.deleteFromSaved(currentItem);
                Toast.makeText(this, "Eltávolítva", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.addToSaved(currentItem);
                Toast.makeText(this, "Mentve", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // A Toolbar vissza gombjának kezelése
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Bezárja az Activity-t
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Belső Adapter osztály a Műfajokhoz (Chip stílus) ---
    private static class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
        private final List<String> genres;

        public GenreAdapter(List<String> genres) {
            this.genres = genres;
        }

        @NonNull
        @Override
        public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Itt egy egyszerű TextView-t hozunk létre programozottan,
            // de használhatsz külön layout fájlt is (pl. item_genre_chip.xml)
            TextView textView = new TextView(parent.getContext());
            textView.setTextColor(parent.getContext().getColor(R.color.core_primary_on));
            textView.setBackgroundResource(R.drawable.background_content_group); // A szürke háttér drawable
            textView.setPadding(30, 15, 30, 15);

            // Margó beállítása
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            textView.setLayoutParams(params);

            return new GenreViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(genres.get(position));
        }

        @Override
        public int getItemCount() {
            return genres.size();
        }

        static class GenreViewHolder extends RecyclerView.ViewHolder {
            public GenreViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}