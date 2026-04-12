package com.example.tvandmovies.UI.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.tvandmovies.model.entities.CastMember;
import com.example.tvandmovies.model.entities.Episode;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.entities.WatchedEpisode;
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

    private List<Episode> allEpisodesOfCurrentSeason = new ArrayList<>();
    private int currentDisplayedEpisodeCount = 0;
    private static final int EPISODES_PER_PAGE = 25;


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

        binding.imdbText.setText(currentItem.getFormatedRating());

        // Dátum formázása
        formatDate(currentItem);

        // Műfajok beállítása (RecyclerView)
        setupGenreList(currentItem.getGenreIds());

        // Mentés gomb (FAB) logika
        setupBookmarkButton();

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
    }

    // dátum formázása
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

    // Ez a függvény true-t ad vissza, ha talál benne orosz, japán, koreai stb. betűt -> le kell kérni az angol szöveget
    private boolean isForeignAlphabet(String text) {
        if (text == null || text.trim().isEmpty()) return false;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                // Ha a betű NEM az alap vagy bővített latin ábécé része (angol/magyar)
                if (block != Character.UnicodeBlock.BASIC_LATIN &&
                        block != Character.UnicodeBlock.LATIN_1_SUPPLEMENT &&
                        block != Character.UnicodeBlock.LATIN_EXTENDED_A &&
                        block != Character.UnicodeBlock.LATIN_EXTENDED_B) {
                    return true; // Találtunk egy idegen karaktert!
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

    // Sorozat epizód beállítása
    private void setupSeriesLogic() {
        EpisodeAdapter episodeAdapter = new EpisodeAdapter(new ArrayList<>());
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

        // deafult szöveg beállítása az 1. évadra
        binding.seasonSelector.setText("1. Évad ▼");

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

        loadSeason(1, adapter); // default 1. évad elő betöltése
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

            // Levágjuk az új, nagyobb adagot
            List<Episode> expandedBatch = new ArrayList<>(allEpisodesOfCurrentSeason.subList(0, currentDisplayedEpisodeCount));
            // Frissítjük a listát
            adapter.setEpisodes(expandedBatch);

            // Ellenőrizzük a gombot (ha elértük a végét, eltüntetjük)
            updateLoadMoreButtonVisibility(adapter);
        });
    }

    // a több megjelenítése gomb láthatóságának beállítása
    private void updateLoadMoreButtonVisibility(EpisodeAdapter adapter) {
        // Ha kevesebb epizódot van betöltve, mint amennyi összesen van, akkor látsszon a gomb
        if (currentDisplayedEpisodeCount < allEpisodesOfCurrentSeason.size()) {
            binding.btnLoadMoreEpisodes.setVisibility(View.VISIBLE);
        } else {
            binding.btnLoadMoreEpisodes.setVisibility(View.GONE);
        }
    }



    // ---- BELSŐ ADAPTEREK -----

    // --- adapter az Epizódokhoz ---
    private class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {
        private List<Episode> episodes;
        private List<WatchedEpisode> watchedEpisodes = new ArrayList<>();

        public EpisodeAdapter(List<Episode> episodes) {
            this.episodes = episodes;
        }

        public void setEpisodes(List<Episode> newEpisodes) {
            this.episodes = newEpisodes;
            notifyDataSetChanged();
        }

        // ezen keresztül kapja meg az adapter az adatbázisból a megnézett részeket
        public void setWatchedEpisodes(List<WatchedEpisode> watchedEpisodes) {
            this.watchedEpisodes = watchedEpisodes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
            return new EpisodeViewHolder(view);
        }

        @SuppressLint("SetTextI18n")  // fordítási figyelmeztetés kikapcsolása
        @Override
        public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
            Episode episode = episodes.get(position);

            // ha hiányzik a magyar cím, akkor a TMDB számozott címet ad vissza, ez nem jó nekem
            String epName = episode.getName() != null ? episode.getName() : "";
            int epNum = episode.getEpisodeNumber();
            if (epName.toLowerCase().contains("epizód") || epName.toLowerCase().contains("episode")) {
                holder.title.setText(epName);
            } else {
                holder.title.setText(epNum + ". " + epName);
            }

            // értékelés pontszám
            holder.episodeRating.setText(episode.getFormatedVoteAVG());

            // epizód hossza percben
            int runtime = episode.getRuntime();
            if (runtime > 1){
                holder.episodeDuration.setText(runtime + " perc");
            } else {
                holder.episodeDuration.setText("-");
            }

            // Premier dátuma (Később ez alapján megy az értesítés)
            holder.date.setText(episode.getAirDate() != null ? episode.getAirDate() : "Nincs dátum");

            // Kép betöltése Glide-dal
            if (episode.getStillUrl() != null) {
                Glide.with(holder.itemView.getContext())
                        .load(episode.getStillUrl())
                        .apply(new RequestOptions().transform(new com.bumptech.glide.load.resource.bitmap.CenterCrop(), new com.bumptech.glide.load.resource.bitmap.RoundedCorners(8)))
                        .into(holder.image);
            } else {
                holder.image.setImageResource(android.R.color.darker_gray); // Ha nincs kép
            }

            // az adott rész már meg van-e nézve?
            boolean isWatched = false;
            WatchedEpisode currentWatchedDbItem = null;

            for (WatchedEpisode w : watchedEpisodes) {
                if (w.getSeasonNumber() == episode.getSeasonNumber() && w.getEpisodeNumber() == episode.getEpisodeNumber()) {
                    isWatched = true;
                    currentWatchedDbItem = w;
                    break;
                }
            }

            // megfelelő állapot beállítása: látta vagy nem látta az adott részt
            if (isWatched) {
                holder.btnWatched.setImageResource(R.drawable.episodeseen);
                holder.itemView.setAlpha(0.6f); // Halványítás
            } else {
                holder.btnWatched.setImageResource(R.drawable.episode_notseen);
                holder.itemView.setAlpha(1.0f); // Normál megjelenés
            }

            boolean finalIsWatched = isWatched;
            WatchedEpisode finalCurrentWatchedDbItem = currentWatchedDbItem;

            // Gomb kattintásának előkészítése (Ide jön majd az adatbázis mentés)
            holder.btnWatched.setOnClickListener(v -> {
                if (finalIsWatched) {
                    // Már meg volt nézve -> Töröljük
                    viewModel.deleteWatchedEpisode(finalCurrentWatchedDbItem);
                    Toast.makeText(holder.itemView.getContext(), "Eltávolítva a már megnézett listából", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(holder.itemView.getContext(), "Megnézve!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return episodes != null ? episodes.size() : 0;
        }

        class EpisodeViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title, date, episodeRating, episodeDuration;
            android.widget.ImageButton btnWatched;

            public EpisodeViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.episodeImage);
                episodeRating = itemView.findViewById(R.id.episodeRating);
                title = itemView.findViewById(R.id.episodeTitle);
                date = itemView.findViewById(R.id.episodeDate);
                btnWatched = itemView.findViewById(R.id.btnWatchedToggle);
                episodeDuration = itemView.findViewById(R.id.episodeDuration);
            }
        }
    }

    // --- adapter a Műfajokhoz (Chip stílus) ---
    private static class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
        private final List<String> genres;

        public GenreAdapter(List<String> genres) {
            this.genres = genres;
        }

        @NonNull
        @Override
        public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextColor(parent.getContext().getColor(R.color.content_card_title));
            textView.setBackgroundResource(R.drawable.background_content_group); // A szürke háttér drawable
            textView.setPadding(35, 18, 35, 18);

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

    // --- adapter a Szereplőkhöz (Cast) ---
    private static class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {
        private final List<CastMember> castList;

        public CastAdapter(List<CastMember> castList) {
            this.castList = castList;
        }

        @NonNull
        @Override
        public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
            return new CastViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
            CastMember castMember = castList.get(position);

            holder.name.setText(castMember.getName());

            // Glide kerekítés varázslat: .circleCrop()
            if (castMember.getProfileUrl() != null) {
                Glide.with(holder.itemView.getContext())
                        .load(castMember.getProfileUrl())
                        .apply(RequestOptions.circleCropTransform()) // TÖKÉLETES KÖR ALAK!
                        .into(holder.image);
            } else {
                // Ha nincs képe a színésznek, beteszünk egy szürke kört
                Glide.with(holder.itemView.getContext())
                        .load(android.R.color.darker_gray)
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.image);
            }
        }

        @Override
        public int getItemCount() {
            // Nem listázzuk ki mind az 50 statisztát, elég az első 15 főszereplő
            return Math.min(castList.size(), 15);
        }

        static class CastViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView name;

            public CastViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.castProfileImage);
                name = itemView.findViewById(R.id.castNameText);
            }
        }
    }
}