package com.example.tvandmovies.UI.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.adapter.CastAdapter;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.UI.adapter.EpisodeAdapter;
import com.example.tvandmovies.UI.adapter.GenreAdapter;
import com.example.tvandmovies.UI.saved.DetailViewModel;
import com.example.tvandmovies.databinding.ActivityContentDetailBinding;
import com.example.tvandmovies.model.entities.CastMember;
import com.example.tvandmovies.model.entities.Episode;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.entities.WatchedEpisode;
import com.example.tvandmovies.repository.ContentRepository;
import com.example.tvandmovies.utilities.FullScreenMode;
import com.example.tvandmovies.utilities.GenreHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityContentDetail extends AppCompatActivity {
    private ActivityContentDetailBinding binding;
    private DetailViewModel viewModel;
    private boolean isBookmarked = false;
    private MediaItem currentItem;
    private YouTubePlayer youTubePlayer;
    private List<Episode> allEpisodesOfCurrentSeason = new ArrayList<>();
    private int currentDisplayedEpisodeCount = 0;
    private static final int EPISODES_PER_PAGE = 25;
    private boolean isInitialSeasonLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Teljes képernyős mód
        FullScreenMode.setupWindowFlags(this);

        // ViewModel inicializálás
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        // Kiszámoljuk az állapotsáv (notch) magasságát
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // Fix margót adunk a YouTube konténernek, hogy a notch alatt kezdődjön a videó
        if (binding.youtubePlayerContainer.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) binding.youtubePlayerContainer.getLayoutParams();
            mlp.topMargin = statusBarHeight;
            binding.youtubePlayerContainer.setLayoutParams(mlp);
        }

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
        Glide.with(this)
                .load(currentItem.getPosterDetailUrl())
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(binding.MediaPoster);

        // Szöveges adatok kinyerése
        String title = currentItem.getTitle();
        String description = currentItem.getDescription();

        // Feltétel: Idegen betűk (pl. orosz) vannak a címben, vagy üres a leírás
        if (isForeignAlphabet(title) || description == null || description.trim().isEmpty()) {

            // Töltő animációk beállítása mindkét mezőre
            binding.mediaDetailTitle.setText("Cím fordítása...");
            binding.mediaDetailTitle.setAlpha(0.6f);

            binding.summaryDescription.setText("Angol nyelvű adatok betöltése...");
            binding.summaryDescription.setAlpha(0.6f);

            viewModel.getEnglishFallback(currentItem.getId(), currentItem.getMediaType())
                    .observe(this, fallbackData -> {
                        // láthatóság visszaállítása
                        binding.mediaDetailTitle.setAlpha(1.0f);
                        binding.summaryDescription.setAlpha(1.0f);

                        if (fallbackData != null) {
                            // content címének beállítása
                            String engTitle = fallbackData.getTitle();
                            if (engTitle != null && !engTitle.trim().isEmpty()) {
                                binding.mediaDetailTitle.setText(engTitle);
                                currentItem.setTitle(engTitle);
                            } else {
                                binding.mediaDetailTitle.setText(title); // ha nem lenne angol cím -> marad az eredeti
                            }

                            // leírás beállítása
                            String engDesc = fallbackData.getOverview();
                            if (engDesc != null && !engDesc.trim().isEmpty()) {
                                binding.summaryDescription.setText(engDesc);
                                currentItem.setDescription(engDesc);
                            } else {
                                binding.summaryDescription.setText("Ehhez a tartalomhoz jelenleg nem érhető el leírás.");
                            }
                        } else {
                            // ha a hálózat hibát dobna
                            binding.mediaDetailTitle.setText(title);
                            binding.summaryDescription.setText("Hiba az adatok betöltésekor.");
                        }
                    });
        } else {
            // ha elérhető magyar/angol cím és van normális leírás is
            binding.mediaDetailTitle.setText(title);
            binding.summaryDescription.setText(description);
        }

        // előzetes play gomb -> youtube előzetes indítása
        binding.btnPlayTrailer.setOnClickListener(v -> {
            com.example.tvandmovies.repository.ContentRepository.getInstance(this)
                    .getTrailerKey(currentItem.getId(), currentItem.getMediaType(), new ContentRepository.TrailerCallback() {

                        @Override
                        public void onResult(String youtubeKey) {
                            if (youtubeKey != null) {
                                //  youtube előzetes lejátszása
                                playYoutubeVideo(youtubeKey);
                            } else {
                                // user figyelmeztetése, ha nincs videó
                                android.widget.Toast.makeText(ActivityContentDetail.this,
                                        "Sajnos nem található előzetes ehhez a tartalomhoz.",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        binding.imdbText.setText(currentItem.getFormatedRating());

        // Dátum formázása
        formatDate(currentItem);

        // Műfajok beállítása (RecyclerView)
        setupGenreList(currentItem.getGenreIds());

        // Mentés gomb (FAB) beállítása
        setupBookmarkButton();

        // Értesítés gomb logika (csak sorozatoknál)
        setupNotificationButton();

        // Szereplők (Cast) betöltése
        viewModel.getCredits(currentItem.getId(), currentItem.getMediaType()).observe(this, creditsResponse -> {
            if (creditsResponse != null && creditsResponse.getCast() != null && !creditsResponse.getCast().isEmpty()) {
                binding.castRecyclerView.setAdapter(new CastAdapter(creditsResponse.getCast()));
            } else {
                // Ha nincsenek szereplők, elrejtjük a címet és a listát is
                binding.castLabel.setVisibility(View.GONE);
                binding.castRecyclerView.setVisibility(View.GONE);
            }
        });

        // ha az adott content nem film, akkor jelenjen meg az epizód lista, különben nem jelenik meg
        if (!"movie".equals(currentItem.getMediaType())){
            binding.seriesContainer.setVisibility(View.VISIBLE);
            setupSeriesLogic();
        } else {
            binding.seriesContainer.setVisibility(View.GONE);
        }

        // Hasonló tartalmak betöltése
        setupRecommendations();
    }

    // ajánlott content megjelenítése
    private void setupRecommendations() {
        ContentAdapter adapter = new ContentAdapter(new ContentAdapter.ContentClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                Intent intent = new Intent(ActivityContentDetail.this, ActivityContentDetail.class);
                intent.putExtra("object", item);
                startActivity(intent);
            }

            @Override
            public void onBookmarkClick(MediaItem item, boolean isBookmarked) {
                if (isBookmarked) {
                    viewModel.addToSaved(item);
                } else {
                    viewModel.deleteFromSaved(item);
                }
            }
        }, ContentAdapter.TYPE_DEFAULT);

        binding.recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recommendationsRecyclerView.setAdapter(adapter);

        // Saját mentett elemek figyelése az adapter szinkronizálásához
        viewModel.getAllSaved().observe(this, savedItems -> {
            if (savedItems != null) {
                adapter.setSavedItems(savedItems);
            }
        });

        viewModel.getRecommendations(currentItem.getId(), currentItem.getMediaType()).observe(this, recommendations -> {
            if (recommendations != null && !recommendations.isEmpty()) {
                binding.recommendationsLabel.setVisibility(View.VISIBLE);
                binding.recommendationsRecyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(recommendations);
            } else {
                binding.recommendationsLabel.setVisibility(View.GONE);
                binding.recommendationsRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void playYoutubeVideo(String videoKey) {
        if (youTubePlayer == null) {
            binding.youtubePlayerContainer.setVisibility(View.VISIBLE);
            
            // a nagy formátumú poszternek fenntartott hely összenyomása
            shrinkAppBarForVideo();

            getLifecycle().addObserver(binding.youtubePlayerView);

            // a beágyazott youtube keretének beállításai
            IFramePlayerOptions options = new IFramePlayerOptions.Builder(ActivityContentDetail.this)
                    .controls(0)      // Eltünteti a gyári YouTube vezérlőket (cím, sáv, gombok)
                    .rel(0)           // Ne ajánljon új videókat a lejátszás végén
                    .ivLoadPolicy(3)  // Eltünteti a felugró videós annotációkat/kártyákat
                    .build();

            binding.youtubePlayerView.initialize(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer player) {
                    youTubePlayer = player;
                    youTubePlayer.loadVideo(videoKey, 0);

                    // UI szinkronizáció
                    binding.MediaPoster.setVisibility(View.GONE);
                    binding.btnPlayTrailer.setVisibility(View.GONE);

                    binding.gradientBottom.setVisibility(View.GONE);
                    binding.gradientTop.setVisibility(View.GONE);
                }
            }, true, options);

        } else {
            shrinkAppBarForVideo();
            youTubePlayer.loadVideo(videoKey, 0);
            binding.MediaPoster.setVisibility(View.GONE);
            binding.btnPlayTrailer.setVisibility(View.GONE);
            binding.youtubePlayerContainer.setVisibility(View.VISIBLE);
            binding.gradientBottom.setVisibility(View.GONE);
            binding.gradientTop.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null && binding.youtubePlayerView != null) {
            binding.youtubePlayerView.release();
        }
    }

    // dátum formázása
    private void formatDate(MediaItem item) {
        if (item == null) return;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());
        try {
            if (item.getReDate() != null) {
                binding.mediaDateAndPlayTime.setText(sdf.format(item.getReDate()));
            } else {
                // Ha az adatbázisban régebbről null maradt, de van címe,
                binding.mediaDateAndPlayTime.setText("Nincs dátum");
            }
        } catch (Exception e) {
            binding.mediaDateAndPlayTime.setText("-");
        }
    }

    // ha tartalmaz a cím idegen nyelvű szöveget, akkor angol forrást használok
    private boolean isForeignAlphabet(String text) {
        if (text == null || text.trim().isEmpty()) return false;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                // Ha a betű nem az alap vagy bővített latin ábécé része (angol/magyar)
                if (block != Character.UnicodeBlock.BASIC_LATIN &&
                        block != Character.UnicodeBlock.LATIN_1_SUPPLEMENT &&
                        block != Character.UnicodeBlock.LATIN_EXTENDED_A &&
                        block != Character.UnicodeBlock.LATIN_EXTENDED_B) {
                    return true;
                }
            }
        }
        return false;
    }

    // műfaj lista beállítása (kódból -> név)
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
                currentItem.setWatched(dbItem.isWatched()); // Szinkronizáljuk a helyi állapotot
                currentItem.setNotificationEnabled(dbItem.isNotificationEnabled()); // Értesítés állapot szinkron
                updateBookmarkIcon();
                updateWatchedToggleIcon();
                updateNotificationIcon();
            } else {
                isBookmarked = false;
                updateBookmarkIcon();
                updateWatchedToggleIcon();
                updateNotificationIcon();
            }
        });

        // Mentés gomb kattintás
        binding.saveMedia.setOnClickListener(v -> {
            if (isBookmarked) {
                viewModel.deleteFromSaved(currentItem);
                Toast.makeText(this, "Eltávolítva a mentettek közül", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.addToSaved(currentItem);
                Toast.makeText(this, "Mentve a könyvtárhoz", Toast.LENGTH_SHORT).show();
            }
        });

        // Megtekintett állapot váltó (szem ikon)
        binding.btnWatchedToggle.setOnClickListener(v -> {
            if (!isBookmarked) {
                // Ha nincs mentve, akkor automatikusan mentjük is, ha megtekintettnek jelöli
                currentItem.setWatched(true);
                viewModel.addToSaved(currentItem);
                Toast.makeText(this, "Mentve és megtekintettnek jelölve", Toast.LENGTH_SHORT).show();
            } else {
                // Ha már mentve van, csak az állapotot váltjuk
                currentItem.setWatched(!currentItem.isWatched());
                viewModel.addToSaved(currentItem);
                String msg = currentItem.isWatched() ? "Megtekintettnek jelölve" : "Megtekintetlennek jelölve";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            updateWatchedToggleIcon();
        });
    }

    private void setupNotificationButton() {
        if ("movie".equals(currentItem.getMediaType())) {
            binding.btnNotificationToggle.setVisibility(View.GONE);
            return;
        }

        binding.btnNotificationToggle.setVisibility(View.VISIBLE);
        updateNotificationIcon();

        binding.btnNotificationToggle.setOnClickListener(v -> {
            if (!isBookmarked) {
                // Ha nincs mentve, előbb elmentjük, hogy legyen hova kapcsolni az értesítést
                currentItem.setNotificationEnabled(true);
                viewModel.addToSaved(currentItem);
                Toast.makeText(this, "Mentve és értesítés bekapcsolva", Toast.LENGTH_SHORT).show();
            } else {
                currentItem.setNotificationEnabled(!currentItem.isNotificationEnabled());
                viewModel.addToSaved(currentItem);
                String msg = currentItem.isNotificationEnabled() ? "Értesítések bekapcsolva" : "Értesítések kikapcsolva";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            updateNotificationIcon();
        });
    }

    private void updateNotificationIcon() {
        if (currentItem != null && currentItem.isNotificationEnabled()) {
            binding.btnNotificationToggle.setImageResource(R.drawable.ic_notifications);
            binding.btnNotificationToggle.setImageTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_blue_light)));
        } else {
            binding.btnNotificationToggle.setImageResource(R.drawable.ic_notifications_off);
            binding.btnNotificationToggle.setImageTintList(ContextCompat.getColorStateList(this, R.color.detail_title));
        }
    }

    private void updateBookmarkIcon() {
        if (isBookmarked) {
            binding.saveMedia.setImageResource(R.drawable.bookmark_filled);
            binding.saveMedia.setImageTintList(ContextCompat.getColorStateList(this, R.color.detail_save_tint));
        } else {
            binding.saveMedia.setImageResource(R.drawable.bookmark_save);
            binding.saveMedia.setImageTintList(ContextCompat.getColorStateList(this, R.color.detail_save_tint));
        }
    }

    private void updateWatchedToggleIcon() {
        if (currentItem != null && currentItem.isWatched()) {
            binding.btnWatchedToggle.setImageResource(R.drawable.episodeseen);
            binding.btnWatchedToggle.setImageTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_green_light)));
        } else {
            binding.btnWatchedToggle.setImageResource(R.drawable.episode_notseen);
            binding.btnWatchedToggle.setImageTintList(ContextCompat.getColorStateList(this, R.color.detail_title));
        }
    }

    // kijelző magasságának csökkentése
    private void shrinkAppBarForVideo() {
        // képernyő szélessége
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Notch magasságának lekérése
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // Kiszámoljuk a pontos 16:9-es magasságot + az állapotsáv helyét
        int targetHeight = (int) (screenWidth * (9.0f / 16.0f)) + statusBarHeight;
        int currentHeight = binding.appBar.getHeight();

        if (currentHeight <= targetHeight + 10) return;

        // Azonnal kinyitjuk (false), hogy ne fusson párhuzamosan két animáció
        binding.appBar.setExpanded(true, false);

        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofInt(currentHeight, targetHeight);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            android.view.ViewGroup.LayoutParams lp = binding.appBar.getLayoutParams();
            lp.height = val;
            binding.appBar.setLayoutParams(lp);
        });

        // Simább interpolátor és rövidebb időtartam a darabosság ellen
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.setDuration(300);
        anim.start();
    }

    // A Toolbar vissza gombjának kezelése
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Sorozat epizód beállítása
    private void setupSeriesLogic() {
        EpisodeAdapter episodeAdapter = new EpisodeAdapter(new ArrayList<>(), (episode, isWatched, watchedDbItem) -> {
            if (isWatched) {
                // Már meg volt nézve -> töröljük
                viewModel.deleteWatchedEpisode(watchedDbItem);
                Toast.makeText(this, "Eltávolítva a már megnézett listából", Toast.LENGTH_SHORT).show();
            } else {
                // Még nem látta -> lehet menteni
                WatchedEpisode newWatched = new WatchedEpisode(
                        "", // A Repository majd kitölti a UserId-t
                        currentItem.getId(),
                        episode.getSeasonNumber(),
                        episode.getEpisodeNumber(),
                        0
                );
                viewModel.insertWatchedEpisode(newWatched, currentItem);
                Toast.makeText(this, "Megnézve!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.episodesRecyclerView.setAdapter(episodeAdapter);

        binding.episodesRecyclerView.setFocusable(false);
        binding.episodesRecyclerView.setNestedScrollingEnabled(false);

        viewModel.getTvSeasonCount(currentItem.getId()).observe(this, seasonCount -> {
            if (seasonCount != null && seasonCount > 0){
                setupSpinner(seasonCount, episodeAdapter);
            }
        });

        // figyeljük a Room adatbázist, hogy miket nézett már meg ebből a sorozatból
        viewModel.getAllWatchedForSeries(currentItem.getId()).observe(this, watchedList -> {
            if (watchedList != null) {
                // Átadjuk az adapternek a friss listát -> a kártyák automatikusan frissülnek
                episodeAdapter.setWatchedEpisodes(watchedList);
            }
        });

        setupLoadMoreButton(episodeAdapter); // "több megjelenítése" gomb beállítása
    }

    // legürdülő évad lista setupolása
    private void setupSpinner(int seasonCount, EpisodeAdapter adapter){
        // egy tömb tárolja az évadok számát
        String[] seasonArray = new String[seasonCount];
        for (int i = 0; i < seasonCount; i++) {
            seasonArray[i] = (i + 1) + ". Évad";
        }

        // Megkeressük a legutóbb megtekintett évadot, hogy azzal nyissunk
        viewModel.getLastWatchedEpisode(currentItem.getId()).observe(this, lastWatched -> {
            if (!isInitialSeasonLoaded) {
                int startSeason = 1;
                if (lastWatched != null && lastWatched.getSeasonNumber() <= seasonCount) {
                    startSeason = lastWatched.getSeasonNumber();
                }

                binding.seasonSelector.setText(startSeason + ". Évad ▼");
                loadSeason(startSeason, adapter);
                isInitialSeasonLoaded = true;
            }
        });

        binding.seasonSelector.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Válassz évadot")
                    .setItems(seasonArray, (dialog, which) -> {
                        int selectedSeason = which + 1;

                        // Frissítjük a gomb szövegét
                        binding.seasonSelector.setText(selectedSeason + ". Évad ▼");

                        // Betöltjük az új évadot
                        loadSeason(selectedSeason, adapter);
                    })
                    .show();
        });
    }

    // Konkrét évad letöltése a ViewModel-en keresztül, 1 oldalon max 25 epizód listázása
    private void loadSeason(int seasonNumber, EpisodeAdapter adapter) {
        viewModel.getSeasonDetails(currentItem.getId(), seasonNumber).observe(this, seasonDetail -> {
            if (seasonDetail != null && seasonDetail.getEpisodes() != null) {

                allEpisodesOfCurrentSeason = seasonDetail.getEpisodes();
                currentDisplayedEpisodeCount = Math.min(EPISODES_PER_PAGE, allEpisodesOfCurrentSeason.size());
                List<Episode> firstBatch = new ArrayList<>(allEpisodesOfCurrentSeason.subList(0, currentDisplayedEpisodeCount));
                adapter.setEpisodes(firstBatch);

                // kell-e a "Mutass többet" gombot megjeleníteni?
                updateLoadMoreButtonVisibility(adapter);
            }
        });
    }

    //a több megjelenítése gomb betöltése
    private void setupLoadMoreButton(EpisodeAdapter adapter) {
        binding.btnLoadMoreEpisodes.setOnClickListener(v -> {
            currentDisplayedEpisodeCount = Math.min(currentDisplayedEpisodeCount + EPISODES_PER_PAGE, allEpisodesOfCurrentSeason.size());

            // Levágjuk az új adagot
            List<Episode> expandedBatch = new ArrayList<>(allEpisodesOfCurrentSeason.subList(0, currentDisplayedEpisodeCount));
            // Frissítjük a listát
            adapter.setEpisodes(expandedBatch);

            // Ellenőrizzük a gombot (ha elértük a végét, eltüntetjük)
            updateLoadMoreButtonVisibility(adapter);
        });
    }

    // a több megjelenítése gomb láthatóságának beállítása
    private void updateLoadMoreButtonVisibility(EpisodeAdapter adapter) {
        // Ha kevesebb epizódot van betöltve, mint amennyi összesen van, akkor legyen látható a gomb
        if (currentDisplayedEpisodeCount < allEpisodesOfCurrentSeason.size()) {
            binding.btnLoadMoreEpisodes.setVisibility(View.VISIBLE);
        } else {
            binding.btnLoadMoreEpisodes.setVisibility(View.GONE);
        }
    }
}
