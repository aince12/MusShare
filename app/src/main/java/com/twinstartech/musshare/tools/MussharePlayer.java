package com.twinstartech.musshare.tools;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.twinstartech.musshare.SplashScreen;
import com.twinstartech.musshare.models.Music;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Siri on 3/19/2017.
 */

public class MussharePlayer {

    private ArrayList<Music> tracks;
    private Context context;
    private MediaPlayer player;
    /**
     * VIEWS
     */
    private SeekBar seekBar;
    private TextView tvDisplayTimers;
    private TextView tvMusicHeaderText;

    private long duration=0;
    private final Handler handler = new Handler();

    private int currentTrackIndex = 0;
    private Music currentTrack;


    private   Runnable UIUpdater = new Runnable() {
        public void run() {
            updateDisplayTimers();
            primarySeekBarProgressUpdater();
        }
    };

    private static final String TAG = "MPlayer";
    public MussharePlayer(Context context, Music music) {
        this.tracks = new ArrayList<>();
        this.tracks.add(music);
        this.context = context;
        this.seekBar = new SeekBar(context);
        this.tvDisplayTimers = new TextView(context);
    }

    public MussharePlayer(Context context, ArrayList<Music> tracks) {
        this.tracks = tracks;
        this.context = context;
        this.seekBar = new SeekBar(context);
        this.tvDisplayTimers = new TextView(context);
    }
    public MussharePlayer setTracks(ArrayList<Music> newTracks){
        this.tracks = newTracks;
        return MussharePlayer.this;
    }

    public MussharePlayer addMusic(Music music){
        this.tracks.add(music);
        return MussharePlayer.this;
    }

    public MussharePlayer setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
        return MussharePlayer.this;
    }

    public MussharePlayer setDisplayTimers(TextView tvDisplayTimers) {
        this.tvDisplayTimers = tvDisplayTimers;
        return MussharePlayer.this;
    }

    public MussharePlayer setMusicHeaderText(TextView tvMusicHeaderText) {
        this.tvMusicHeaderText = tvMusicHeaderText;
        return MussharePlayer.this;
    }

    public MussharePlayer play(){

        if(player!=null){

            player.start();
        }
        else {

            try {
                player = new MediaPlayer();
                currentTrack = this.tracks.get(currentTrackIndex);
                player.setDataSource(context, Uri.parse(currentTrack.url));

                tvMusicHeaderText.setText("Loading: " + currentTrack.title + " by " + currentTrack.artist);
                tvMusicHeaderText.setSelected(true);

                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        duration = player.getDuration();
                        tvMusicHeaderText.setText("Now Playing: " + currentTrack.title + " by " + currentTrack.artist);
                        tvMusicHeaderText.setSelected(true);

                        player.start();
                        primarySeekBarProgressUpdater();
                    }
                });
                player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                        Log.e(TAG, "Buffering @ " + i + "%");
                        seekBar.setSecondaryProgress(i);
                    }
                });
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        playNext();
                    }
                });


                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return this;
    }

    private void primarySeekBarProgressUpdater() {
        seekBar.setProgress((int)(((float)player.getCurrentPosition()/duration)*100)); // This math construction give a percentage of "was playing"/"song length"
        if (player.isPlaying()) {
            handler.postDelayed(UIUpdater,1000);
        }
    }

    void updateDisplayTimers(){
        tvDisplayTimers.setText(convertSecondsToHMmSs(player.getCurrentPosition())+"/"+convertSecondsToHMmSs(duration));
    }

    private static String convertSecondsToHMmSs(long seconds) {

        seconds = seconds /1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        if(h!=0)
        return String.format("%d:%02d:%02d", h,m,s);
        else
        return String.format("%02d:%02d",m,s);
    }

    public void playNext(){
        if(this.tracks.size()<=(currentTrackIndex + 1)) {
            Log.e(TAG,"Cant play next..index out of range (size: "+this.tracks.size()+" index+1: "+(currentTrackIndex+1));
            return;
        }
        else{
            closeCurrentMusic();
            currentTrackIndex++;
            play();
        }
    }
    public void playPrevious(){
        if(currentTrackIndex==0){
            Log.e(TAG,"Can't play previous..already first track");
            return;
        }
        else{
            closeCurrentMusic();
            currentTrackIndex--;
            play();
        }
    }

    public void pause(){
        if(player!=null){
            if(player.isPlaying())
            player.pause();
        }
    }
    public void stop(){
        closeCurrentMusic();
    }
    private void closeCurrentMusic(){
        if(player   !=null){
            if(player.isPlaying())
                player.stop();
            handler.removeCallbacks(UIUpdater);
            player.release();

            tvDisplayTimers.setText("00:00/00:00");
            seekBar.setProgress(0);
            seekBar.setSecondaryProgress(0);
        }
        player = null;
    }

}
