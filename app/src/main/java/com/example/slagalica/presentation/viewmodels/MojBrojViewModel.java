package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.service.match.MojBrojService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;
import lombok.Setter;

@HiltViewModel
public class MojBrojViewModel extends ViewModel {
    private final MojBrojService gameService;

    @Inject
    public MojBrojViewModel(MojBrojService mojBrojService){
        this.gameService = mojBrojService;
    }


    @Getter
    private int goalNumber = 0;

    @Getter
    private int[] singleDigits = {};
    @Getter
    private int[] doubleDigits = {};

    @Getter @Setter
    private String opponentAnswer = "25 x 2";
    @Getter @Setter
    private int myNumber = 315;
    @Getter @Setter
    private int opponentNumber = 44;

    private final MutableLiveData<Boolean> isGoalSpinning = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> areOperandsSpinning = new MutableLiveData<>(true);

    private final MutableLiveData<Boolean> isCorrect = new MutableLiveData<>();
    public LiveData<Boolean> getIsCorrect() { return isCorrect; }

    public void checkAnswer(List<String> tokens) {
        isCorrect.setValue(true);
    }

    public LiveData<Boolean> getIsGoalSpinning(){
        return isGoalSpinning;
    }

    public int generateGoalNumber(){
        goalNumber = gameService.generateGoalNumber();
        return goalNumber;
    }


    public int[] generateOperands(){
        int[] numbers = gameService.generateOperands();
        singleDigits = new int[]{numbers[0], numbers[1], numbers[2], numbers[3]};
        doubleDigits = new int[]{numbers[4], numbers[5]};

        return numbers;
    }

    public void stopGoalSpinning(){
        isGoalSpinning.setValue(false);
    }

    public LiveData<Boolean> getAreOperandsSpinning(){
        return areOperandsSpinning;
    }

    public void stopOperandsSpinning(){
        areOperandsSpinning.setValue(false);
    }
}
