package com.example.tvandmovies;

import static org.junit.Assert.assertEquals;

import com.example.tvandmovies.cloud.CloudPaths;

import org.junit.Test;

public class CloudPathsTest {

    @Test
    public void watchedEpisodeDocId_shouldFormatSeriesSeasonEpisode() {
        assertEquals("131265_S1_E2", CloudPaths.watchedEpisodeDocId(131265, 1, 2));
    }

    @Test
    public void watchedEpisodeDocId_shouldKeepZeroValues() {
        assertEquals("5_S0_E0", CloudPaths.watchedEpisodeDocId(5, 0, 0));
    }
}
