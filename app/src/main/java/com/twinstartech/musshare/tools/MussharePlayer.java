package com.twinstartech.musshare.tools;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.squareup.picasso.Picasso;
import com.twinstartech.musshare.R;
import com.twinstartech.musshare.models.Music;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Siri on 3/19/2017.
 */

public class MussharePlayer {


    private static final String TAG = "MPlayer";

    private ArrayList<Music> tracks;
    private Context context;
    private static MediaPlayer player;
    /**
     * VIEWS
     */
    private SeekBar seekBar;
    private TextView tvDisplayTimers;
    private TextView tvMusicHeaderText;

    private ImageView ivPrevious;
    private ImageView ivNext;
    private ImageView ivPlayPause;



    private long duration=0;
    private final Handler handler = new Handler();

    private int currentTrackIndex = 0;
    private Music currentTrack;
    private boolean isCurrentTrackPrepared = false;


    private   Runnable UIUpdater = new Runnable() {
        public void run() {
            updateDisplayTimers();
            primarySeekBarProgressUpdater();
        }
    };


    /**
     * Headset/Blutooth speaker buttons
     */
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;





    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            float percentage = ( (float)seekBar.getProgress() /100.0f);
            long seekTime = (long) (duration * percentage);
            tvDisplayTimers.setText(convertSecondsToHMmSs(seekTime)+"/"+convertSecondsToHMmSs(duration));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeCallbacks(UIUpdater);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {//received is the percentage of the seekbar progress
            float percentage = ( (float)seekBar.getProgress() /100.0f);
            float seekTime = duration * percentage;
            player.seekTo((int)seekTime);
            handler.postDelayed(UIUpdater,1000);
        }
    };

    public MussharePlayer(Context context, Music music) {
        this.tracks = new ArrayList<>();
        this.tracks.add(music);
        this.context = context;
        this.seekBar = new SeekBar(context);
        this.tvDisplayTimers = new TextView(context);
        initMediaRemote();
    }

    public MussharePlayer(Context context, ArrayList<Music> tracks) {
        this.tracks = tracks;
        this.context = context;
        this.seekBar = new SeekBar(context);
        this.tvDisplayTimers = new TextView(context);
        initMediaRemote();
    }
    public MussharePlayer setTracks(ArrayList<Music> newTracks){
        this.tracks = newTracks;
        this.updatePlaybackControls();
        return MussharePlayer.this;
    }

    public MussharePlayer addMusic(Music music){
        this.tracks.add(music);
        this.updatePlaybackControls();
        return MussharePlayer.this;
    }

    public MussharePlayer setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
        this.seekBar.setMax(100);
        this.seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
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

    public MussharePlayer setPlaybackControls(ImageView ivPrevious, ImageView ivPlayPause, ImageView ivNext){
        this.ivPrevious = ivPrevious;
        this.ivPlayPause = ivPlayPause;
        this.ivNext = ivNext;
        return MussharePlayer.this;
    }

    public MussharePlayer play(){
        if(player!=null){

            if(isCurrentTrackPrepared) {
                player.start();
                updatePlaybackControls();
            }
        }
        else {

            try {
                isCurrentTrackPrepared = false;
                player = new MediaPlayer();
                currentTrack = this.tracks.get(currentTrackIndex);

                //TODO ENABLE CACHING ON RELEASE

                 HttpProxyCacheServer cache = ProxyFactory.getProxy(context);
                  String proxyUri = cache.getProxyUrl(currentTrack.url);
                  player.setDataSource(proxyUri);




//                player.setDataSource(context, Uri.parse(currentTrack.url));

                updatePlaybackControls();

                tvMusicHeaderText.setText("Loading: " + currentTrack.title + " by " + currentTrack.artist);
                tvMusicHeaderText.setSelected(true);

                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        duration = player.getDuration();
                        tvMusicHeaderText.setText("Now Playing: " + currentTrack.title + " by " + currentTrack.artist);
                        tvMusicHeaderText.setSelected(true);
                        isCurrentTrackPrepared = true;
                        player.start();
                        primarySeekBarProgressUpdater();
                        updatePlaybackControls();
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
        if(!canPlayNext()) {
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
        if(!canPlayPrevious()){
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
            if(player.isPlaying()) {
                player.pause();
                updatePlaybackControls();
            }
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


    private void updatePlaybackControls(){
        /**
         * Play/Pause Button
         */
        if(ivPlayPause!=null)
        if(player.isPlaying()){
            Picasso.with(context).load(R.mipmap.ic_pause_circle_outline_white).into(ivPlayPause);
        }
        else
            Picasso.with(context).load(R.mipmap.ic_play_circle_outline_white).into(ivPlayPause);

        /**
         * Next Button
         */
        if(ivNext!=null)
        if(canPlayNext()){
            ivNext.setVisibility(View.VISIBLE);
            Picasso.with(context).load(R.mipmap.ic_skip_next_white).into(ivNext);
        }
        else
            ivNext.setVisibility(View.INVISIBLE);
        /**
         * Previous Button
         */
        if(ivPrevious!=null)
            if(canPlayPrevious()){
                ivPrevious.setVisibility(View.VISIBLE);
                Picasso.with(context).load(R.mipmap.ic_skip_previous_white).into(ivPrevious);
            }
            else
                ivPrevious.setVisibility(View.INVISIBLE);
    }

    /**
     *
     * Boolean methods
     */
    private boolean canPlayNext(){
        return this.tracks.size()>(currentTrackIndex + 1);
    }

    private boolean canPlayPrevious(){
        return currentTrackIndex>0;
    }



    /**
     *
     *
     * METHODS TO MONITOR REMOTE DEVICE
     *
     */



    private void initMediaRemote(){
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(context,
                HeadsetButtonReceiver.class.getName());

        Log.e(TAG,"REGISTERED MEDIA REMOTE");
        mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
        Constants.currentPlayer = MussharePlayer.this;
    }


    /**
     * Destroy method must be called to unregister
     * the audio manager broadcast receiver
     *
     */
    public void onDestroy(){
        if(mAudioManager!=null)
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

}
