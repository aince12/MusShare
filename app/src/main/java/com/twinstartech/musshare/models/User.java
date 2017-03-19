package com.twinstartech.musshare.models;

/**
 * Created by Siri on 3/19/2017.
 */

public class User {
    public String id;
    public String name;
    public String email;

    public User() {
        this.id = "";
        this.name = "";
        this.email = "";
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
