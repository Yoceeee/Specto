package com.example.tvandmovies.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.tvandmovies.R;
import com.example.tvandmovies.api.RetrofitClient;
import com.example.tvandmovies.api.TMDbApi;
import com.example.tvandmovies.database.AppDatabase;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.model.responses.TvDetailResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

public class SeriesNotificationWorker extends Worker {
    private static final String CHANNEL_ID = "series_notifications";

    public SeriesNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return Result.success();

        // 1. Lekérjük a Room-ból azokat a sorozatokat, amiknél be van kapcsolva az értesítés
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        List<MediaItem> seriesList = db.savedContentDao().getContinueWatchingSeriesSync(userId);

        if (seriesList == null || seriesList.isEmpty()) return Result.success();

        // Javított API elérés: a getClient().create(TMDbApi.class) használatával
        TMDbApi api = RetrofitClient.getClient().create(TMDbApi.class);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (MediaItem item : seriesList) {
            try {
                Response<TvDetailResponse> response = api.getTvSeriesDetails(item.getId()).execute();
                if (response.isSuccessful() && response.body() != null) {
                    TvDetailResponse detail = response.body();
                    
                    if (detail.getNextEpisodeToAir() != null) {
                        String airDate = detail.getNextEpisodeToAir().getAirDate();
                        
                        if (today.equals(airDate)) {
                            sendNotification(detail.getName(), detail.getNextEpisodeToAir().getEpisodeNumber(), detail.getNextEpisodeToAir().getSeasonNumber());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Result.success();
    }

    private void sendNotification(String seriesName, int episodeNum, int seasonNum) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Sorozat emlékeztetők", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        String message = String.format(Locale.getDefault(), "Ma érkezik a(z) %s új része! (S%d E%d)", seriesName, seasonNum, episodeNum);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.play_circle)
                .setContentTitle("Új epizód érkezett!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(seriesName.hashCode(), builder.build());
    }
}