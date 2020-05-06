package com.darthwithap.homeapp;

public class BookingModelTech {
    String Name, Time;

    public BookingModelTech() {
    }

    public BookingModelTech(String name, String time) {
        Name = name;
        Time = time;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getName() {
        return Name;
    }

    public String getTime() {
        return Time;
    }
}

