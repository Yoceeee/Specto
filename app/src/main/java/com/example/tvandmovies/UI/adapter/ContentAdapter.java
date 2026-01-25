package com.example.tvandmovies.UI.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.databinding.ContentCardBinding;
import com.example.tvandmovies.databinding.ItemSearchBinding;
import com.example.tvandmovies.model.MediaItem;

public class ContentAdapter extends ListAdapter<MediaItem, RecyclerView.ViewHolder> {
    private static final int TYPE_DEFAULT = 0; //(home)
    private static final int TYPE_SEARCH = 1; // (explore, keresés)
    private final int layoutType; // 0 = item_movie, 1 = item_search
    private final ContentClickListener clickListener;

    // kattintás kezelés interface segítségével
    public interface ContentClickListener{
        void onItemClick(MediaItem item); // kártyára kattintás -> részletek activity
        void onBookmarkClick(MediaItem item); // Adott kártya mentése
    }

    public ContentAdapter(ContentClickListener clickListener, boolean isSearchLayout){
        super(DIFF_CALLBACK);
        this.layoutType = isSearchLayout ? TYPE_SEARCH : TYPE_DEFAULT;
        this.clickListener = clickListener;
    }

    // adatfrissítés, most már a DiffUtil használatával
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
    // TODO: majd elrakni egy utils mappába


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
            ((SearchViewHolder) holder).bind(item, clickListener);
        } else if (holder instanceof ContentViewHolder) {
            ((ContentViewHolder) holder).bind(item, clickListener);
        }
    }


    // VIEW HOLDERS
    //A főképernyőn megjelenő kártyákat állítja be
    static class ContentViewHolder extends RecyclerView.ViewHolder {
        private final ContentCardBinding binding;
        private boolean isBookmarked = false; // TODO: majd később a mentéshez

        public ContentViewHolder(ContentCardBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        // A használni kívánt adatok beállítása bindelés használatával
        public void bind(MediaItem mediaItem, ContentClickListener listener){
            binding.textTitle.setText(mediaItem.getTitle() != null ? mediaItem.getTitle() : "No title");
            binding.mediaGenre.setText(mediaItem.getDescription() != null ? mediaItem.getDescription() : "No description");
            binding.imdbScore.setText(mediaItem.getFormatedRating());

            // TODO: Szavazatok (pl. formázva: 1200 -> 1.2k) - ezt majd később
            // binding.voteCount.setText(...);

            binding.btnBookmark.setOnClickListener(v -> {
                // TODO: adatbázisba mentés majd, illetve lehessen unsave-elni
                //Vizuális visszajelzés, hogy mentve lett
                binding.btnBookmark.setImageResource(R.drawable.bookmark_filled);
                Toast.makeText(v.getContext(), "Sikeresen mentve: " + mediaItem.getTitle(), Toast.LENGTH_SHORT).show();
            });

            int targetWidth = 340;
            int targetHeight = 500;

            // Kép betöltése
            Glide.with(binding.getRoot().getContext())
                    .load(mediaItem.getFullPosterUrl())
                    .override(targetWidth, targetHeight)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // cach-el mindent
                            .dontAnimate()  // Nincs fade-in animáció, simább görgetés
                            .bitmapTransform(new RoundedCorners(16)))
                    .into(binding.imagePoster);

            // Kattintáskezelés, a detail nézet megnyitásához
            binding.getRoot().setOnClickListener(view -> {
                if (listener != null) listener.onItemClick(mediaItem);
            });
        }
    }

    // Kereső (explorer) nézet (activity) beallítása
    static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final ItemSearchBinding binding;

        public SearchViewHolder(ItemSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MediaItem mediaItem, ContentClickListener listener) {
            binding.textTitle.setText(mediaItem.getTitle() != null ? mediaItem.getTitle() : "No title");

            Glide.with(binding.getRoot().getContext())
                    .load(mediaItem.getFullPosterUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(12)))
                    .into(binding.imagePoster);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(mediaItem);
            });
        }
    }
}