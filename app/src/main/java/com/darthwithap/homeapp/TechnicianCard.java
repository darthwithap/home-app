package com.darthwithap.homeapp;

public class TechnicianCard {
    String Job, Name, Email, Username;
    Double Rating;
    public TechnicianCard() {
    }

    public TechnicianCard(String job, String name, Double rating, String email, String username) {
        Email=email;
        Username=username;
        Job = job;
        Name = name;
        Rating = rating;
    }

    public void setJob(String job) {
        Job = job;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public void setRating(Double rating) {
        Rating = rating;
    }

    public String getJob() {
        return Job;
    }

    public String getEmail() {
        return Email;
    }

    public String getUsername() {
        return Username;
    }

    public String getName() {
        return Name;
    }

    public Double getRating() { return Rating;}
}
