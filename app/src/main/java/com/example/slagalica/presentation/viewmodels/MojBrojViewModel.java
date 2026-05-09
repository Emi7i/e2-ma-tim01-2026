package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;
import lombok.Setter;

@HiltViewModel
public class MojBrojViewModel extends ViewModel {
    @Inject
    public MojBrojViewModel(){}

    @Getter
    private final int goalNumber = 314;

    @Getter
    private final int[] singleDigits = {1, 2, 3, 4};
    @Getter
    private final int[] doubleDigits = {25, 50};

    @Getter @Setter
    private String opponentAnswer = "25 x 2";
    @Getter @Setter
    private int myNumber = 315;
    @Getter @Setter
    private int opponentNumber = 44;

    private final MutableLiveData<Boolean> isCorrect = new MutableLiveData<>();
    public LiveData<Boolean> getIsCorrect() { return isCorrect; }

    public void checkAnswer(List<String> tokens) {
        isCorrect.setValue(true);
    }
}
