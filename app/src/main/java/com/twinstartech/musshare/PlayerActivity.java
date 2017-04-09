package com.twinstartech.musshare;

import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twinstartech.musshare.models.Music;
import com.twinstartech.musshare.tools.Constants;
import com.twinstartech.musshare.tools.MussharePlayer;
import com.twinstartech.musshare.tools.notificationplayer.NotificationService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayerActivity extends RootActivity {


    DatabaseReference database;

    @BindView(R.id.tvNowPlaying)
    TextView tvNowPlaying;

    @BindView(R.id.tvDisplayTimers)
    TextView tvDisplayTimers;

    @BindView(R.id.sbPlayerTracker)
    SeekBar sbPlayerTrack;


    @BindView(R.id.btnNext)
    ImageButton btnNext;
    @BindView(R.id.btnPlayPause)
    ImageButton btnPlayPause;
    @BindView(R.id.btnPrevious)
    ImageButton btnPrevious;

    private MussharePlayer player;

    /**
     *  Media device commands listener, ex: headset
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        startPlaying();
    }






    @OnClick(R.id.btnNext)
    void next(){
        if(player!=null)
            player.playNext();
    }
    @OnClick(R.id.btnPrevious)
    void previous(){
        if(player!=null)
            player.playPrevious();
    }
    @OnClick(R.id.btnPlayPause)
    void playPause(){
        Log.e(TAG,"PlayPause fired");
        if(player!=null)
        if(player.isPlaying())
            player.pause();
        else
            player.play();
    }

    void startPlaying(){

        database = FirebaseDatabase.getInstance().getReference();

        database.child(Constants.TABLE_MUSIC_LIB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    Constants.allMusic.clear();
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Music music= postSnapshot.getValue(Music.class);
                        Constants.allMusic.add(music);
                        Log.e(TAG,"NEW MUSIC : "+music.title);
                    }
                    if(player!=null){
                        Log.e(TAG,"Updated new tracks: size: "+Constants.allMusic.size());
                        player.setTracks(Constants.allMusic);
                    }
                    else {
                        player = new MussharePlayer(PlayerActivity.this, Constants.allMusic)
                                .setSeekBar(sbPlayerTrack)
                                .setMusicHeaderText(tvNowPlaying)
                                .setDisplayTimers(tvDisplayTimers)
                                .setPlaybackControls(btnPrevious,btnPlayPause, btnNext)
                                .play();

                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    @Override
    protected void onDestroy() {
        if(player!=null)
            player.onDestroy();

        super.onDestroy();
    }


    protected void onPause() {


        Intent service = new Intent(PlayerActivity.this, NotificationService.class);
        service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(service);



        if(player!=null){
            player.isActivityPaused(true);
        }


        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        player = Constants.currentPlayer;

        if(player!=null){

            player.setSeekBar(sbPlayerTrack)
                    .setMusicHeaderText(tvNowPlaying)
                    .setDisplayTimers(tvDisplayTimers)
                    .setPlaybackControls(btnPrevious,btnPlayPause, btnNext);

                player.isActivityPaused(false);
        }
    }
}
