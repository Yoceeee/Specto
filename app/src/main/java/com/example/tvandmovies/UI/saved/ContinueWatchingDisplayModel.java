package com.example.tvandmovies.UI.saved;

import com.example.tvandmovies.model.entities.MediaItem;

public class ContinueWatchingDisplayModel {
    private final MediaItem mediaItem;
    private final String statusText;

    public ContinueWatchingDisplayModel(MediaItem mediaItem, String statusText) {
        this.mediaItem = mediaItem;
        this.statusText = statusText;
    }

    public MediaItem getMediaItem() { return mediaItem; }
    public String getStatusText() { return statusText; }
}
