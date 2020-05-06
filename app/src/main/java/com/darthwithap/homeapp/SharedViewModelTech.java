package com.darthwithap.homeapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModelTech extends ViewModel {
    MutableLiveData<String> category = new MutableLiveData<>();
    MutableLiveData<String> job = new MutableLiveData<>();

    public void setCategory(String Category) {
        category.setValue(Category);
    }

    public LiveData<String> getCategory() {
        return this.category;
    }

    public MutableLiveData<String> getJob() {
        return this.job;
    }

    public void setJob(String Job) { job.setValue(Job);
    }
}
