package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorak;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

@HiltViewModel
public class KorakPoKorakViewModel extends ViewModel {
    @Getter
    private KorakPoKorak game;
    private CountDownTimer timer;

    private static final long REVEAL_PAUSE_MS = 8000L;

    @Inject
    public KorakPoKorakViewModel() {}

    @Getter
    private final int[] points = {20, 18, 16, 14, 12, 10, 8};


    // hardcoded for now - stands in for "is it my turn" check
    private static final long LOGGED_IN_USER_ID = 1L;

    private final MutableLiveData<String> latestHint = new MutableLiveData<>();
    private final MutableLiveData<Boolean> revealAllHints = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> stealWindowOpen = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> gameOver = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> timeLeft = new MutableLiveData<>();
    public LiveData<Integer> getTimeLeft() { return timeLeft; }
    public LiveData<String> getLatestHint() { return latestHint; }
    public LiveData<Boolean> getRevealAllHints() { return revealAllHints; }
    public LiveData<Boolean> getStealWindowOpen() { return stealWindowOpen; }
    public LiveData<Boolean> getGameOver() { return gameOver; }

    public void start(KorakPoKorak game) {
        if (this.game == game) return;
        this.game = game;
        game.startNewRound(() -> {
            String currentHint = game.getHints().get(0);
            latestHint.postValue(currentHint);
        });
        startTimer();
    }

    private boolean isMyTurn() {
        // TODO: BAD
//        return game.getCurrentPlayer() == LOGGED_IN_USER_ID;
        return true;
    }


    private void startTimer() {
        long roundLengthMs = game.getRoundLength() * 1000L;
        long secondsPerHintMs = KorakPoKorak.SECONDS_PER_ANSWER * 1000L;

        timer = new CountDownTimer(roundLengthMs, 1000) {
            long elapsedSeconds = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                elapsedSeconds++;
                timeLeft.postValue((int) (millisUntilFinished / 1000));
                if (isMyTurn() && elapsedSeconds % KorakPoKorak.SECONDS_PER_ANSWER == 0) {
                    String hint = game.revealNextHint();
                    latestHint.postValue(hint);
                }
            }

            @Override
            public void onFinish() {
                if (isMyTurn()) {
                    if (!game.isStealOpportunity()) {
                        // round timed out with no correct answer - open steal window for other player
                        game.openStealOpportunity();
                        stealWindowOpen.postValue(true);
                        startStealTimer();
                    } else {
                        // steal window also expired - move to next round
                        advanceRound();
                    }
                }
            }
        };
        timer.start();
    }

    public List<String> getAllHints(){
        return game.getHints();
    }

    private void startStealTimer() {
        timer = new CountDownTimer(KorakPoKorak.SECONDS_PER_ANSWER * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.postValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (isMyTurn()) {
                    revealAllHints.postValue(true);
                    advanceRound();
                }
            }
        };
        timer.start();
    }

    private void advanceRound() {
        new CountDownTimer(REVEAL_PAUSE_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.postValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timeLeft.postValue(0);
                game.startNewRound(() -> {
                    if (!game.hasEnded()) {
                        latestHint.postValue(game.getHints().get(0));
                    }
                });
                if (game.hasEnded()) {
                    gameOver.postValue(true);
                } else {
                    revealAllHints.postValue(false);
                    stealWindowOpen.postValue(false);
                    startTimer();
                }
            }
        }.start();
    }

    /**
     * Called by fragment when user submits an answer.
     */
    public boolean submitAnswer(String answer) {
        boolean correct = game.isAnswerCorrect(answer);
        if (correct) {
            timer.cancel();
            revealAllHints.postValue(true);
            advanceRound();
        }
        // if incorrect: nothing happens, fragment shows red flash, timer keeps running
        return correct;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
    }
}
