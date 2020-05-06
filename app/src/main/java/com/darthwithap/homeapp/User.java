package com.darthwithap.homeapp;

public class User {

    public String username, uid, type;

    public User() {
    }

    public User(String uid) {
        this.uid=uid;
    }

    public String getType() {
        return type;
    }

    public User(String uid, String type) {
        this.uid=uid;
        this.type=type;
    }

    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }


}
