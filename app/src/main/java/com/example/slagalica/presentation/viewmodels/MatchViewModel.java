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

    private final MutableLiveData<Boolean> isGameActive = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> player1Score = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> player2Score = new MutableLiveData<>(0);

    public LiveData<Boolean> getIsGameActive() { return isGameActive; }
    public LiveData<Integer> getPlayer1Score() { return player1Score; }
    public LiveData<Integer> getPlayer2Score() { return player2Score; }

    public void setGameActive(boolean active) { isGameActive.setValue(active); }

    public void updatePlayer1Score(int delta) {
        Integer current = player1Score.getValue();
        player1Score.setValue((current != null ? current : 0) + delta);
    }

    public void updatePlayer2Score(int delta) {
        Integer current = player2Score.getValue();
        player2Score.setValue((current != null ? current : 0) + delta);
    }
}
