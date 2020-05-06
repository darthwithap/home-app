package com.darthwithap.homeapp;

public class ReviewCard {
    String Name, Review;

    public ReviewCard(){ }

    public ReviewCard(String name, String review){
        Name=name;
        Review=review;
    }

    public void setName(String name){
        Name=name;
    }

    public void setReview(String review){
        Review=review;
    }

    public String getName(){
        return Name;
    }

    public String getReview(){
        return Review;
    }
}

