package com.example.tvandmovies.cloud;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A Firestore útvonalak egy helyen. Így a séma változtatásakor nem kell
 * több fájlban keresgélni a string literálokat.
 */
public final class CloudPaths {

    private CloudPaths() {}

    /** users/{uid}/saved_content/{mediaId} */
    public static DocumentReference savedContent(String uid, int mediaId) {
        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("saved_content")
                .document(String.valueOf(mediaId));
    }

    /** Egyedi azonosító egy megnézett epizódhoz, pl. 131265_S1_E2 */
    public static String watchedEpisodeDocId(int seriesId, int seasonNumber, int episodeNumber) {
        return seriesId + "_S" + seasonNumber + "_E" + episodeNumber;
    }
}
