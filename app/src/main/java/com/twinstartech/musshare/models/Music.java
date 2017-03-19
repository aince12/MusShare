package com.twinstartech.musshare.models;

/**
 * Created by Siri on 3/19/2017.
 */

public class Music {

    public String title;
    public String artist;
    public String url;
    public long music_id;

    public Music() {
        this.title = "";
        this.artist = "";
        this.url = "";
        this.music_id = 0;
    }

    public Music(String title, String artist, String url, long music_id) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.music_id = music_id;
    }
}
