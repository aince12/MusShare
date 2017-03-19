package com.twinstartech.musshare;

import android.content.Intent;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.twinstartech.musshare.models.Music;
import com.twinstartech.musshare.models.User;
import com.twinstartech.musshare.tools.Constants;
import com.twinstartech.musshare.tools.MussharePlayer;

public class SplashScreen extends RootActivity {


    @BindView(R.id.btnLogin)
    Button btnLogin;

    /**
     * FIREBASE VARIABLES
     *
     */
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authListener;


    /**
     * FACEBOOK
     */
    CallbackManager callbackManager;


    DatabaseReference database;

    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ButterKnife.bind(this);

        /**
         * FIREBASE SETUP
         */
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG,loginResult.getAccessToken().getToken());
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG,error.getLocalizedMessage());
            }
        });


        /**
         *
         * Check if facebook already logged in
         *
         */

        AccessToken token = AccessToken.getCurrentAccessToken();
        if(token != null ){
            if(!TextUtils.isEmpty(token.getToken())) {
                Log.e(TAG, "Already loggedin!");
//                LoginManager.getInstance().logOut();
                handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
            }
            else  {
                btnLogin.setVisibility(View.VISIBLE);
            }
        } else  {
            btnLogin.setVisibility(View.VISIBLE);
        }
    }


    /**
     * START FACEBOOK LOGIN
     */
    @OnClick(R.id.btnLogin)
    void login(){
        LoginManager.getInstance().logInWithReadPermissions(SplashScreen.this, Arrays.asList("public_profile","email"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    /***
     *
     */

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SplashScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else if(task.isSuccessful()){

                            checkLoginDetails();
                        }
                    }
                });

    }


    void checkLoginDetails(){
        database = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser u =  FirebaseAuth.getInstance().getCurrentUser();
        uid = u.getUid();

        /***
         *
         * Check if user already added on the database
         *
         */


        /***
         *
         * Check if user already added on the database
         *
         */

        database.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //check if user exists in firebase db
                if(dataSnapshot.exists()){
                    // if user does exist, move along to home page
                    Log.e(TAG, "USER ALREADY EXISTS..proceeding...");
                    User data = dataSnapshot.getValue(User.class);
                    Constants.currentUser = data;
                    showHome(false);
                }
                else{
                    //otherwise create new user record on firebase

                    Log.e(TAG, "USER DOESNT EXISTS..creating...");

                    database.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            User data = dataSnapshot.getValue(User.class);
                            database.child("users").child(uid).removeEventListener(this);
                            database.child("users").child(uid)
                                    .child("createDate")
                                    .setValue(ServerValue.TIMESTAMP);
                            database.child("users").child(uid)
                                    .child("photoUrl")
                                    .setValue(auth.getCurrentUser().getPhotoUrl().toString());

                            Constants.currentUser = data;
                            showHome(true);


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG,"Failed sign in. "+databaseError.getDetails());
                        }
                    });


                    //create new user on firebase db
                    database.child("users").child(uid).setValue(new User(uid,u.getDisplayName(),u.getEmail()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    void showHome(boolean isNew){

        String welcomeHeader;
        if(isNew)
            welcomeHeader = "Welcome to "+TAG;
        else
            welcomeHeader = "Welcome back";

        Toast.makeText(SplashScreen.this,
                welcomeHeader+" "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                Toast.LENGTH_SHORT)
                .show();
        startActivity(new Intent(SplashScreen.this, PlayerActivity.class));
        finishAffinity();
    }

}
