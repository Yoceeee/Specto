package com.example.tvandmovies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.tvandmovies.utilities.Converter;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ConverterTest {

    @Test
    public void toDate_shouldConvertLongToDate() {
        // Arrange : előkészítés -> adat megadása
        Long timestamp = 1000L;

        // Act: a függvény meghívása
        Date result = Converter.toDate(timestamp);

        // Assert: ellenőrzés
        assertEquals(new Date(1000L), result);
    }

    @Test
    public void toTimestamp_shouldConvertDateToLong() {
        Date date = new Date(2000L);
        Long result = Converter.toTimestamp(date);
        assertEquals(Long.valueOf(2000L), result);
    }

    // null handling test
    @Test
    public void toDate_nullInput_shouldReturnNull() {
        assertNull(Converter.toDate(null));
    }

    @Test
    public void toTimestamp_nullInput_shouldReturnNull() {
        assertNull(Converter.toTimestamp(null));
    }

    @Test
    public void fromIntegerList_shouldConvertListToJson() {
        List<Integer> input = Arrays.asList(28, 12);

        String result = Converter.fromIntegerList(input);

        assertEquals("[28,12]", result);
    }

    @Test
    public void toIntegerList_shouldConvertJsonToList() {
        String json = "[28,12]";

        List<Integer> result = Converter.toIntegerList(json);

        assertEquals(Arrays.asList(28, 12), result);
    }

    @Test
    public void toIntegerList_null_shouldReturnEmptyList() {
        List<Integer> result = Converter.toIntegerList(null);

        assertTrue(result.isEmpty());
    }
}


