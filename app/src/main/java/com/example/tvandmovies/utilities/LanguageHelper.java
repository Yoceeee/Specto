package com.example.tvandmovies.utilities;

public class LanguageHelper {
    // a számunkra olvashatatlan pl orosz vagy távol-keleti szövegek esetén le kell kérni az angol fordítást
    public static boolean isForeignAlphabet(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
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
}