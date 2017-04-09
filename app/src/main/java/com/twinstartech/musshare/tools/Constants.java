package com.twinstartech.musshare.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.twinstartech.musshare.R;
import com.twinstartech.musshare.models.Music;
import com.twinstartech.musshare.models.User;

/**
 * Created by Siri on 3/19/2017.
 */

public class Constants {

    public static String TABLE_MUSIC_LIB = "musics";

    public static ArrayList<Music> allMusic = new ArrayList<>();
    public static User currentUser;


    public static MussharePlayer currentPlayer= null;


    public static void logoutCurrentUser(){

        FirebaseAuth.getInstance().signOut();

        //if signed in using facebook;
        LoginManager.getInstance().logOut();
    }

    /**\
     *
     * Notification player variables
     *
     */

    public static interface ACTION{
        public static String MAIN_ACTION = "com.mushare.customnotification.action.main";
        public static String INIT_ACTION = "com.mushare.customnotification.action.init";
        public static String PREV_ACTION = "com.mushare.customnotification.action.prev";
        public static String PLAY_ACTION = "com.mushare.customnotification.action.play";
        public static String NEXT_ACTION = "com.mushare.customnotification.action.next";
        public static String STARTFOREGROUND_ACTION = "com.mushare.customnotification.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.mushare.customnotification.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_headphone, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

}

