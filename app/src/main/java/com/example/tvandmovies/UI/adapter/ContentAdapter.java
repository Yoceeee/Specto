package com.example.tvandmovies.UI.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.ContentCardBinding;
import com.example.tvandmovies.databinding.ItemSearchBinding;
import com.example.tvandmovies.model.MediaItem;
import com.example.tvandmovies.utilities.GenreHelper;

public class ContentAdapter extends ListAdapter<MediaItem, RecyclerView.ViewHolder> {
    private static final int TYPE_DEFAULT = 0; //(home)
    private static final int TYPE_SEARCH = 1; // (explore, keresés)
    private final int layoutType; // 0 = item_movie, 1 = item_search

    // a már mentett contentek listája
    private Set<Integer> savedIds = new HashSet<>();
    private final ContentClickListener clickListener;

    // kattintás kezelés interface segítségével
    public interface ContentClickListener{
        void onItemClick(MediaItem item); // kártyára kattintás -> részletek activity
        void onBookmarkClick(MediaItem item, boolean isCurrentlySaved); // Adott kártya mentése saját listába
    }

    public ContentAdapter(ContentClickListener clickListener, boolean isSearchLayout){
        super(DIFF_CALLBACK);
        this.layoutType = isSearchLayout ? TYPE_SEARCH : TYPE_DEFAULT;
        this.clickListener = clickListener;
    }

    // adatfrissítés, DiffUtil használatával
    private static final DiffUtil.ItemCallback<MediaItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<MediaItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    // a megfelelő layout típus visszaadása
    @Override
    public int getItemViewType(int position) {
        return layoutType;
    }

    // a mentett Contentek updatelése (csak az ikon, a többi dologhoz nem nyúl) + értesítés
    public void setSavedItems(List<MediaItem> savedItems){
        this.savedIds.clear();
        for (MediaItem item : savedItems){
            this.savedIds.add(item.getId());
        }
        // adapter értesítése a gomb állapotának változásáról
        notifyItemRangeChanged(0, getItemCount(), "UPDATE_ICONS");
    }

    // ViewHolder létrehozása
    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //ha a recyclerView keresés típusú, akkor a vízszintes recycler-t használjuk
        if (viewType == TYPE_SEARCH) {
            ItemSearchBinding binding = ItemSearchBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new SearchViewHolder(binding);
        } else {
            //ha pedig a "hagyományos" típus, akkor a függőleges kártyákat tölti be
            ContentCardBinding binding = ContentCardBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new ContentViewHolder(binding);
        }
    }

    // adott content bindelése a megfelelő "kártyára", a megfelelő nézet szerint
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MediaItem item = getItem(position);

        if (holder instanceof SearchViewHolder) {
            ((SearchViewHolder) holder).bind(item, clickListener, savedIds);
        } else if (holder instanceof ContentViewHolder) {
            ((ContentViewHolder) holder).bind(item, clickListener, savedIds);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        MediaItem item = getItem(position);
        // Megnézzük, érkezett-e "UPDATE_ICONS" üzenet
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload.equals("UPDATE_ICONS")) {
                    // Ha igen, akkor CSAK az ikont frissítjük!
                    if (holder instanceof ContentViewHolder) {
                        ((ContentViewHolder) holder).updateIconState(savedIds);
                    } else if (holder instanceof SearchViewHolder) {
                        //((SearchViewHolder) holder).currentItem(savedIds);
                    }
                    return;
                }
            }
        }

        // Ha nincs payload (vagy nem ismert), akkor  teljes újrarajzolás
        onBindViewHolder(holder, position);
    }


    // VIEW HOLDER
    //A főképernyőn megjelenő kártyákat állítja be
    static class ContentViewHolder extends RecyclerView.ViewHolder {
        private final ContentCardBinding binding;
        private MediaItem currentItem;
        public ContentViewHolder(ContentCardBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        // A használni kívánt adatok beállítása bindelés használatával
        public void bind(MediaItem mediaItem, ContentClickListener listener, Set<Integer> savedIds){
            this.currentItem = mediaItem;

            binding.textTitle.setText(mediaItem.getTitle() != null ? mediaItem.getTitle() : "Nincs címe");

            binding.mediaGenre.setText(mediaItem.getFormatedGenre());

            binding.imdbScore.setText(mediaItem.getFormatedRating());
            binding.voteCount.setText(mediaItem.getFormatedVoteCount());

            // "mentés" ikon beállítása
            updateIconState(savedIds);

            binding.btnBookmark.setOnClickListener(v -> {
                if (listener != null){
                    boolean isSaved = savedIds.contains(mediaItem.getId());
                    listener.onBookmarkClick(mediaItem, isSaved);
                }
            });

            int targetWidth = 340;
            int targetHeight = 500;
            Glide.with(binding.getRoot().getContext())
                    .load(mediaItem.getPosterThumbUrl())
                    .override(targetWidth, targetHeight)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // cach-el mindent
                            .dontAnimate()  // Nincs fade-in animáció, simább görgetés
                            .transform(new RoundedCorners(16)))
                    .into(binding.imagePoster);

            // Kattintáskezelés, a detail activity megnyitásához
            binding.getRoot().setOnClickListener(view -> {
                if (listener != null) listener.onItemClick(mediaItem);
            });
        }

        // könyvjelző állapotának updatelése
        public void updateIconState(Set<Integer> savedIds) {
            if (currentItem == null) return;
            boolean isSaved = savedIds.contains(currentItem.getId());
            binding.btnBookmark.setImageResource(isSaved ? R.drawable.bookmark_filled : R.drawable.bookmark_save);
        }
    }

    // Kereső (explorer) nézet (activity) beallítása
    static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final ItemSearchBinding binding;
        private MediaItem currentItem;

        public SearchViewHolder(ItemSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MediaItem mediaItem, ContentClickListener listener, Set<Integer> savedIds) {
            this.currentItem = mediaItem;
            binding.textTitle.setText(mediaItem.getTitle() != null ? mediaItem.getTitle() : "No title");

            // Értékelés
            binding.textRating.setText(mediaItem.getFormatedRating());

            // megjelenés évszáma
            if (mediaItem.getReDate() != null) {
                // Csak az évet kérjük el a formázótól
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                binding.textYear.setText(yearFormat.format(mediaItem.getReDate()));
            } else {
                binding.textYear.setText("-");
            }

            // műfaj megjelenítési beállításai
            if (mediaItem.getGenreIds() != null && !mediaItem.getGenreIds().isEmpty()){
                List<String> genreNames = new ArrayList<>();
                // max 3 műfaj kiírása
                int limit = Math.min(mediaItem.getGenreIds().size(), 3);

                for (int i = 0; i < limit; i++){
                    int genreId = mediaItem.getGenreIds().get(i);
                    String name = GenreHelper.getGenreName(genreId);
                    if (!name.isEmpty()){
                        genreNames.add(name);
                    }
                }
                // műfajok összefűzése
                binding.textDescription.setText(String.join(", ", genreNames));
            }else{
                binding.textDescription.setText("nem található műfaj");
            }

            // konyvjelző állapota
            updateIconState(savedIds);

            binding.btnBookmarkSearch.setOnClickListener(v -> {
                if (listener != null){
                    boolean isSaved = savedIds.contains(mediaItem.getId());
                    listener.onBookmarkClick(mediaItem, isSaved);
                }
            });

            Glide.with(binding.getRoot().getContext())
                    .load(mediaItem.getPosterThumbUrl())
                    .apply(new RequestOptions()
                            .transform(new CenterCrop(), new RoundedCorners(12))) // Egységes Glide
                    .into(binding.imagePoster);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(mediaItem);
            });

        }
        // updateli a könyvjező állapotát
        public void updateIconState(Set<Integer> savedIds) {
            if (currentItem == null) return;
            boolean isSaved = savedIds.contains(currentItem.getId());
            binding.btnBookmarkSearch.setImageResource(isSaved ? R.drawable.bookmark_filled : R.drawable.bookmark_save);
        }
    }
}