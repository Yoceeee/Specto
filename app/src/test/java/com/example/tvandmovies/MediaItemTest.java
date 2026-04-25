package com.example.tvandmovies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.utilities.Converter;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MediaItemTest {
    @Test
    public void formatVoteCount_lessThanThousand() {
        MediaItem item = new MediaItem();
        item.setVote_count(850);
        String result = item.getFormatedVoteCount();
        assertEquals("(850)", result);
    }

    @Test
    public void formatVoteCount_exactThousand() {
        MediaItem item = new MediaItem();
        item.setVote_count(1000);

        String result = item.getFormatedVoteCount();

        assertEquals("(1 k)", result);
    }

    @Test
    public void formatVoteCount_thousands() {
        MediaItem item = new MediaItem();
        item.setVote_count(1500);
        String result = item.getFormatedVoteCount();
        assertEquals("(1.5 k)", result);
    }

    @Test
    public void formatVoteCount_millions() {
        MediaItem item = new MediaItem();
        item.setVote_count(2500000);
        String result = item.getFormatedVoteCount();
        assertEquals("(2.5 M)", result);
    }

    @Test
    public void formatGenre_emptyList() {
        MediaItem item = new MediaItem();
        item.setGenreIds(Collections.emptyList());
        String result = item.getFormatedGenre();
        assertEquals("Nem található műfaj", result);
    }

    @Test
    public void formatVoteCount_zero() {
        MediaItem item = new MediaItem();
        item.setVote_count(0);

        String result = item.getFormatedVoteCount();

        assertEquals("(0)", result);
    }

    @Test
    public void fromIntegerList_emptyList() {
        List<Integer> input = Collections.emptyList();

        String result = Converter.fromIntegerList(input);

        assertEquals("[]", result);
    }

    @Test
    public void getFormatedGenre_emptyList_shouldReturnDefaultText() {
        MediaItem item = new MediaItem();
        item.setGenreIds(new java.util.ArrayList<>());
        assertEquals("Nem található műfaj", item.getFormatedGenre());
    }

    @Test
    public void getFormatedVoteCount_exactlyOneThousand_shouldReturn1k() {
        MediaItem item = new MediaItem();
        item.setVote_count(1000);
        assertEquals("(1 k)", item.getFormatedVoteCount());
    }

    @Test
    public void getFormatedVoteCount_exactlyOneMillion_shouldReturn1M() {
        MediaItem item = new MediaItem();
        item.setVote_count(1000000);
        assertEquals("(1 M)", item.getFormatedVoteCount());
    }

    @Test
    public void getFormatedRating_zeroRating_shouldReturnFormattedZero() {
        MediaItem item = new MediaItem();
        item.setVote_avg(0.0);
        assertEquals("0.0", item.getFormatedRating());
    }
}
