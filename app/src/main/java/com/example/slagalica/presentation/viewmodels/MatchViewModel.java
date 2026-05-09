package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchViewModel extends ViewModel {
    @Inject
    public MatchViewModel() {}

    // TODO: Add getters and setters for stars (points) and timer
    private final MutableLiveData<Boolean> isGameActive = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsGameActive() { return isGameActive; }

    public void setGameActive(boolean active) { isGameActive.setValue(active); }

}
