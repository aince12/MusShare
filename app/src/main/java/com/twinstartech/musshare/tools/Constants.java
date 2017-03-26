package com.twinstartech.musshare.tools;

import java.util.ArrayList;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
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
}

