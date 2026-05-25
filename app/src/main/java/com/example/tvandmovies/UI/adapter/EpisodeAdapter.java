package com.example.tvandmovies.UI.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.model.entities.Episode;
import com.example.tvandmovies.model.entities.WatchedEpisode;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {
    private List<Episode> episodes;
    private List<WatchedEpisode> watchedEpisodes = new ArrayList<>();
    private final OnEpisodeClickListener listener;

    public interface OnEpisodeClickListener {
        void onWatchedClick(Episode episode, boolean isWatched, WatchedEpisode watchedDbItem);
    }

    public EpisodeAdapter(List<Episode> episodes, OnEpisodeClickListener listener) {
        this.episodes = episodes;
        this.listener = listener;
    }

    public void setEpisodes(List<Episode> newEpisodes) {
        this.episodes = newEpisodes;
        notifyDataSetChanged();
    }

    public void setWatchedEpisodes(List<WatchedEpisode> watchedEpisodes) {
        this.watchedEpisodes = watchedEpisodes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode episode = episodes.get(position);

        String epName = episode.getName() != null ? episode.getName() : "";
        int epNum = episode.getEpisodeNumber();
        if (epName.toLowerCase().contains("epizód") || epName.toLowerCase().contains("episode")) {
            holder.title.setText(epName);
        } else {
            holder.title.setText(epNum + ". " + epName);
        }

        holder.episodeRating.setText(episode.getFormatedVoteAVG());

        int runtime = episode.getRuntime();
        if (runtime > 1) {
            holder.episodeDuration.setText(runtime + " perc");
        } else {
            holder.episodeDuration.setText("-");
        }

        holder.date.setText(episode.getAirDate() != null ? episode.getAirDate() : "Nincs dátum");

        if (episode.getStillUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(episode.getStillUrl())
                    .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(8)))
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.boritokep);
        }

        boolean isWatched = false;
        WatchedEpisode currentWatchedDbItem = null;

        for (WatchedEpisode w : watchedEpisodes) {
            if (w.getSeasonNumber() == episode.getSeasonNumber() && w.getEpisodeNumber() == episode.getEpisodeNumber()) {
                isWatched = true;
                currentWatchedDbItem = w;
                break;
            }
        }

        if (isWatched) {
            holder.btnWatched.setImageResource(R.drawable.episodeseen);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.btnWatched.setImageResource(R.drawable.episode_notseen);
            holder.itemView.setAlpha(1.0f);
        }

        boolean finalIsWatched = isWatched;
        WatchedEpisode finalCurrentWatchedDbItem = currentWatchedDbItem;

        holder.btnWatched.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWatchedClick(episode, finalIsWatched, finalCurrentWatchedDbItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return episodes != null ? episodes.size() : 0;
    }

    static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView date;
        final TextView episodeRating;
        final TextView episodeDuration;
        final ImageButton btnWatched;

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
