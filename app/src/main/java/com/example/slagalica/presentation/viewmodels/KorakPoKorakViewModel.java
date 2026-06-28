package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorak;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

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

    private final UserStatisticsRepository statsRepository;

    private String getCurrentUserId() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    private int roundsPlayed = 0;
    private int correctRounds = 0;

    @Inject
    public KorakPoKorakViewModel(UserStatisticsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

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
        roundsPlayed = 0;
        game.startNewRound(() -> {
            String currentHint = game.getHints().get(0);
            latestHint.postValue(currentHint);
            roundsPlayed++;
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
                        roundsPlayed++;
                    }
                });
                if (game.hasEnded()) {
                    gameOver.postValue(true);
                    updateUserStatistics();
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
            correctRounds++;
            timer.cancel();
            revealAllHints.postValue(true);
            advanceRound();
        }
        // if incorrect: nothing happens, fragment shows red flash, timer keeps running
        return correct;
    }

    private boolean statsUpdated = false;

    public void updateUserStatistics() {
        if (statsUpdated) return;
        statsUpdated = true;
        String userId = getCurrentUserId();
        statsRepository.getStatistics(userId).thenAccept(stats -> {
            UserStatistics finalStats = (stats != null) ? stats : UserStatistics.createNew(userId);
            
            int gameTotal = roundsPlayed;
            int gameCorrect = correctRounds;
            double oldAccuracy = finalStats.getKorakPoKorak();

            // Track points and game count
            // Since we don't have easy access to round points here directly, we'll use a placeholder or check session
            // For now, let's assume we can't easily get the delta here, so we'll just increment played.
            // Actually, we can get it from the game object if we had a getter, but we don't.
            // Let's just track the session count.
            finalStats.setKorakPoKorakPlayed(finalStats.getKorakPoKorakPlayed() + 1);

            // Step-based success tracking
            if (correctRounds > 0 && game != null) {
                // Find which hint was last revealed (0-indexed)
                int stepIndex = game.getCurrentHint() - 1; 
                if (stepIndex >= 0 && stepIndex < 7) {
                    List<Long> steps = finalStats.getKorakPoKorakStepSuccessCount();
                    if (steps == null || steps.size() < 7) {
                        steps = new java.util.ArrayList<>(java.util.Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L));
                    } else {
                        steps = new java.util.ArrayList<>(steps);
                    }
                    steps.set(stepIndex, steps.get(stepIndex) + 1);
                    finalStats.setKorakPoKorakStepSuccessCount(steps);
                }
            }

            // Weighted Accuracy Update (100% for step 1, 80% for step 2, etc.)
            double weightedCorrect = 0;
            if (correctRounds > 0 && game != null) {
                int step = game.getCurrentHint(); // 1-7
                weightedCorrect = 1.0 - ((step - 1) * 0.15);
            }

            long newTotal = finalStats.getKorakPoKorakTotal() + gameTotal;
            // Since we need to store as long, we'll store scaled or keep raw accuracy but weighted
            // For now, let's update the percentage directly using weighted logic
            double totalWeightedSuccess = (oldAccuracy / 100.0 * finalStats.getKorakPoKorakTotal()) + weightedCorrect;
            finalStats.setKorakPoKorakTotal(newTotal);
            if (newTotal > 0) {
                finalStats.setKorakPoKorak(totalWeightedSuccess / newTotal * 100.0);
            }
            double newAccuracy = finalStats.getKorakPoKorak();

            Log.d("UserStats", String.format("Stats changed: Korak po korak - Game Correct: %d/%d | Total Accuracy: %.1f%% -> %.1f%%",
                    gameCorrect, gameTotal, oldAccuracy, newAccuracy));
            finalStats.calculateOverallStats();
            statsRepository.saveStatistics(finalStats).exceptionally(e -> {
                Log.e("UserStats", "Failed to save statistics", e);
                return null;
            });
        }).exceptionally(e -> {
            Log.e("UserStats", "Failed to get statistics", e);
            return null;
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
    }
}
