package com.example.tvandmovies.UI.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tvandmovies.UI.saved.ContinueWatchingDisplayModel;
import com.example.tvandmovies.databinding.ItemContinueWatchingBinding;
import com.example.tvandmovies.model.entities.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class ContinueWatchingAdapter extends RecyclerView.Adapter<ContinueWatchingAdapter.ViewHolder> {

    private List<ContinueWatchingDisplayModel> displayList = new ArrayList<>();
    private final OnContinueWatchingListener listener;

    public interface OnContinueWatchingListener {
        void onItemClick(MediaItem item);
    }

    public ContinueWatchingAdapter(OnContinueWatchingListener listener) {
        this.listener = listener;
    }

    // A ViewModelből érkező már összefésült listát itt vesszük át
    public void setSeries(List<ContinueWatchingDisplayModel> newList) {
        this.displayList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContinueWatchingBinding binding = ItemContinueWatchingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(displayList.get(position));
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContinueWatchingBinding binding;

        public ViewHolder(ItemContinueWatchingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ContinueWatchingDisplayModel model) {
            MediaItem item = model.getMediaItem();

            // Alapadatok beállítása
            binding.tvSeriesTitle.setText(item.getTitle());
            binding.tvNextEpisode.setText(model.getStatusText());

            // Poszter betöltése
            Glide.with(itemView.getContext())
                    .load(item.getBackdropUrl())
                    .centerCrop()
                    .into(binding.ivPoster);

            // Kattintás kezelése
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}