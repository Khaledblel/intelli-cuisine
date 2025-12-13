package com.khaled.intellicuisine.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.ui.dashboard.CookingModeActivity;

import java.util.Locale;

public class TimerService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_STOP = "ACTION_STOP";
    
    public static final String BROADCAST_TICK = "com.khaled.intellicuisine.TIMER_TICK";
    public static final String BROADCAST_FINISH = "com.khaled.intellicuisine.TIMER_FINISH";
    public static final String EXTRA_TIME_LEFT = "EXTRA_TIME_LEFT";
    public static final String EXTRA_STEP_INDEX = "EXTRA_STEP_INDEX";

    private static final String CHANNEL_ID = "CookingTimerChannel";
    private static final int NOTIFICATION_ID = 101;

    private final IBinder binder = new LocalBinder();
    private CountDownTimer timer;
    
    private long timeLeftInMillis = 0;
    private long totalDurationInMillis = 0;
    private boolean isTimerRunning = false;
    private int currentStepIndex = -1;
    private String recipeTitle = "";

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PAUSE:
                    pauseTimer();
                    break;
                case ACTION_RESUME:
                    startTimer(timeLeftInMillis, currentStepIndex, recipeTitle);
                    break;
                case ACTION_STOP:
                    stopTimer();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startTimer(long duration, int stepIndex, String title) {
        stopTimer();
        
        this.totalDurationInMillis = duration;
        this.timeLeftInMillis = duration;
        this.currentStepIndex = stepIndex;
        this.recipeTitle = title;
        this.isTimerRunning = true;

        startForeground(NOTIFICATION_ID, createNotification());

        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                broadcastUpdate(BROADCAST_TICK);
                updateNotification();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isTimerRunning = false;
                broadcastUpdate(BROADCAST_FINISH);
                showFinishedNotification();
                stopForeground(true);
            }
        }.start();
    }

    public void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isTimerRunning = false;
        updateNotification();
        broadcastUpdate(BROADCAST_TICK);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = 0;
        currentStepIndex = -1;
        stopForeground(true);
        broadcastUpdate(BROADCAST_TICK);
    }

    public boolean isRunning() {
        return isTimerRunning;
    }

    public long getTimeLeft() {
        return timeLeftInMillis;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_TIME_LEFT, timeLeftInMillis);
        intent.putExtra(EXTRA_STEP_INDEX, currentStepIndex);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Minuteur de Cuisine",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, CookingModeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, TimerService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pPauseIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent resumeIntent = new Intent(this, TimerService.class);
        resumeIntent.setAction(ACTION_RESUME);
        PendingIntent pResumeIntent = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pStopIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", 
                (timeLeftInMillis / 1000) / 60, (timeLeftInMillis / 1000) % 60);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(recipeTitle.isEmpty() ? "Minuteur" : recipeTitle)
                .setContentText("Étape " + (currentStepIndex + 1) + " : " + timeFormatted)
                .setSmallIcon(R.drawable.ic_time)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        if (isTimerRunning) {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pPauseIntent);
        } else {
            builder.addAction(android.R.drawable.ic_media_play, "Reprendre", pResumeIntent);
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Arrêter", pStopIntent);

        return builder.build();
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private void showFinishedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("C'est prêt !")
                .setContentText("Le minuteur de l'étape " + (currentStepIndex + 1) + " est terminé.")
                .setSmallIcon(R.drawable.ic_time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID + 1, builder.build());
        }
    }
}