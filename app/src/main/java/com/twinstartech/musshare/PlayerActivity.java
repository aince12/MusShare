package com.twinstartech.musshare;

import android.os.Bundle;
import android.util.Log;
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

import java.util.Random;

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


    private MussharePlayer player;

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
                                .play();

                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
