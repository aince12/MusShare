package com.twinstartech.musshare.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by Siri on 3/26/2017.
 */

public class HeadsetButtonReceiver extends BroadcastReceiver {


    private static String TAG = "Headset";
    private MussharePlayer player;
    public HeadsetButtonReceiver() {
        this.player = Constants.currentPlayer;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.e("BROADCAST","ACTION: "+intentAction);
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            Log.i ("BROADCAST", "no media button information");
            return;
        }
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if(event!=null){
            Log.e(TAG,">"+event.getAction()+" >"+KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }

        if (event == null) {
            Log.i ("BROADCAST", "no keypress");
        }
        else if(event.getAction() == KeyEvent.ACTION_UP){
            /**
             * No use in proceeding if we cant access
             *  Musshare player instance
             */
            player = Constants.currentPlayer;
            if(player==null)
                return;

            int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    // code for fast forward
                    Log.e(TAG,"FAST FORWARD");
                    //do nothing
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    // code for next
                    Log.e(TAG,"NEXT");
                    player.playNext();

                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    // code for previous
                    Log.e(TAG,"PREVIOUS");
                    player.playPrevious();
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    // code for rewind
                    Log.e(TAG,"REWIND");
                    //do nothing
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    // code for stop
                    Log.e(TAG,"STOP");
                    player.stop();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: //85
                default:
                    // code for play/pause
                    Log.e(TAG,"PLAY/PAUSE");

                    if(player .isPlaying())
                        player.pause();
                    else
                        player.play();

                    break;
            }


        }
    }
}
