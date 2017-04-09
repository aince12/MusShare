package com.twinstartech.musshare.tools.notificationplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.twinstartech.musshare.PlayerActivity;
import com.twinstartech.musshare.R;
import com.twinstartech.musshare.tools.Constants;
import com.twinstartech.musshare.tools.MussharePlayer;

/**
 * Created by Siri on 4/9/2017.
 */

public class NotificationService extends Service {


    private MussharePlayer player;
    RemoteViews views;
    RemoteViews bigViews;
    public NotificationService(){

        this.player = Constants.currentPlayer;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            showNotification();
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "Clicked Previous");
            player.playPrevious();

            updateDisplays();
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "Clicked Play");
            if(player.isPlaying())
                player.pause();
            else
            player.play();


            updateDisplays();
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "Clicked Next");
            player.playNext();
            updateDisplays();
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
            player.stop();
            player.onDestroy();
        }


        return START_STICKY;
    }


    Notification status;
    private final String LOG_TAG = "NotificationService";

    private void showNotification() {
// Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

// showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, NotificationService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, NotificationService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, NotificationService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        views.setImageViewResource(R.id.status_bar_play,
                R.mipmap.ic_pause_circle_outline_white);
        bigViews.setImageViewResource(R.id.status_bar_play,
                R.mipmap.ic_pause_circle_outline_white);



        views.setTextViewText(R.id.status_bar_track_name, player.getCurrentTrackTitle());
        bigViews.setTextViewText(R.id.status_bar_track_name, player.getCurrentTrackTitle());

        views.setTextViewText(R.id.status_bar_artist_name, player.getCurrentTrackArtist());
        bigViews.setTextViewText(R.id.status_bar_artist_name, player.getCurrentTrackArtist());

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name");

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_launcher;
        status.contentIntent = pendingIntent;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        updateDisplays();
    }

    private void updateDisplays(){

        views= status.contentView;
        bigViews = status.bigContentView;
        Log.e(LOG_TAG, "updating info: "+player.getCurrentTrackTitle());

        views.setTextViewText(R.id.status_bar_track_name, player.getCurrentTrackTitle() );
        bigViews.setTextViewText(R.id.status_bar_track_name, player.getCurrentTrackTitle());

        views.setTextViewText(R.id.status_bar_artist_name, player.getCurrentTrackArtist());
        bigViews.setTextViewText(R.id.status_bar_artist_name, player.getCurrentTrackArtist());

        if(player.isPlaying()){
            views.setImageViewResource(R.id.status_bar_play,
                    R.mipmap.ic_pause_circle_outline_white);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.mipmap.ic_pause_circle_outline_white);
        }
        else{
            views.setImageViewResource(R.id.status_bar_play,
                    R.mipmap.ic_play_circle_outline_white);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.mipmap.ic_play_circle_outline_white);
        }

        if(player.isNextAllowed()){

            views.setImageViewResource(R.id.status_bar_next, R.mipmap.ic_skip_next_white);
            bigViews.setImageViewResource(R.id.status_bar_next,  R.mipmap.ic_skip_next_white);
        }
        else{

            views.setImageViewBitmap(R.id.status_bar_next, null);
            bigViews.setImageViewBitmap(R.id.status_bar_next,  null);
        }

        if(player.isPreviousAllowed()){

            views.setImageViewResource(R.id.status_bar_prev, R.mipmap.ic_skip_previous_white);
            bigViews.setImageViewResource(R.id.status_bar_prev,  R.mipmap.ic_skip_previous_white);
        }
        else{

            views.setImageViewBitmap(R.id.status_bar_prev, null);
            bigViews.setImageViewBitmap(R.id.status_bar_prev,  null);
        }

    }
}
