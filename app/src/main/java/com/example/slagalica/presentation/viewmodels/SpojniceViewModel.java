package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.slagalica.domain.model.config.SpojniceConfig;
import com.example.slagalica.domain.model.match.games.Spojnice;
import com.example.slagalica.domain.model.match.games.SpojniceSessionData;
import com.example.slagalica.domain.model.progression.UserStatistics;
import com.example.slagalica.domain.service.match.SpojniceDemoFactory;
import com.example.slagalica.repository.impl.SpojniceRepository;
import com.example.slagalica.repository.impl.SpojniceSessionRepository;
import com.example.slagalica.repository.impl.UserStatisticsRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Setter;

@HiltViewModel
public class SpojniceViewModel extends ViewModel {

    private final SpojniceRepository repository;
    private final UserStatisticsRepository statsRepository;
    private final SpojniceSessionRepository spojniceSessionRepository;
    private String getCurrentUserId() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    
    private final MutableLiveData<List<Spojnice>> allSpojnice = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentRound = new MutableLiveData<>(1);
    private final MutableLiveData<Spojnice> currentData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> leftColumn = new MutableLiveData<>();
    private final MutableLiveData<List<String>> rightColumn = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPlayerTurn = new MutableLiveData<>(1); // 1 or 2
    private final MutableLiveData<Integer> startingPlayerOfRound = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> currentLeftIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Map<Integer, Integer>> player1Matches = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<Integer, Integer>> player2Matches = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<Integer, Integer>> missedMatches = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Integer> p1RoundScore = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> p2RoundScore = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> p1ScoreDelta = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> p2ScoreDelta = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> timeLeft = new MutableLiveData<>(SpojniceConfig.TIME_PER_PLAYER_SECONDS);
    private final MutableLiveData<Boolean> gameFinished = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> waitingForNextPlayer = new MutableLiveData<>(false);

    private CountDownTimer timer;
    private int player1TotalScore = 0;
    private int player1CorrectAccumulated = 0;
    private final Map<String, String> pairMap = new HashMap<>(); // Question -> Answer

    @Setter
    private String matchId;

    @Inject
    public SpojniceViewModel(
            SpojniceRepository repository,
            com.example.slagalica.repository.impl.UserStatisticsRepository statsRepository,
            SpojniceSessionRepository spojniceSessionRepository) {
        this.repository = repository;
        this.statsRepository = statsRepository;
        this.spojniceSessionRepository = spojniceSessionRepository;
        loadData();
    }

    public LiveData<Spojnice> getCurrentData() { return currentData; }
    public LiveData<List<String>> getLeftColumn() { return leftColumn; }
    public LiveData<List<String>> getRightColumn() { return rightColumn; }
    public LiveData<Integer> getCurrentPlayerTurn() { return currentPlayerTurn; }
    public LiveData<Integer> getCurrentLeftIndex() { return currentLeftIndex; }
    public LiveData<Map<Integer, Integer>> getPlayer1Matches() { return player1Matches; }
    public LiveData<Map<Integer, Integer>> getPlayer2Matches() { return player2Matches; }
    public LiveData<Map<Integer, Integer>> getMissedMatches() { return missedMatches; }
    public LiveData<Integer> getTimeLeft() { return timeLeft; }
    public LiveData<Boolean> isGameFinished() { return gameFinished; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> isWaitingForNextPlayer() { return waitingForNextPlayer; }
    public LiveData<Integer> getCurrentRound() { return currentRound; }
    public LiveData<Integer> getP1ScoreDelta() { return p1ScoreDelta; }
    public LiveData<Integer> getP2ScoreDelta() { return p2ScoreDelta; }

    private void loadData() {
        isLoading.setValue(true);
        repository.getRandomSpojnice(SpojniceConfig.ROUNDS_COUNT).thenAccept(loaded -> {
            if (loaded == null || loaded.isEmpty()) {
                Log.e("SpojniceViewModel", "No data found in Firestore!");
                isLoading.postValue(false);
                return;
            }
            allSpojnice.postValue(loaded);
            isLoading.postValue(false);
            startRound(1, loaded);
        }).exceptionally(ex -> {
            Log.e("SpojniceViewModel", "Error loading Spojnice data", ex);
            isLoading.postValue(false);
            return null;
        });
    }

    private void startRound(int round, List<Spojnice> dataList) {
        Log.d("Spojnice", "Round: " + round);

        currentRound.postValue(round);

        Log.d("Spojnice", "Current round: " + currentRound.getValue());

        int startingPlayer = (round == 1) ? 1 : 2;
        startingPlayerOfRound.postValue(startingPlayer);
        
        if (dataList != null && round <= dataList.size()) {
            Spojnice data = dataList.get(round - 1);
            currentData.postValue(data);
            updateSessionData(dataList);
            setupRoundData(data);
            startPlayerTurn(startingPlayer);
        } else {
            Log.d("Spojnice", "Rounds exhausted");
            gameFinished.postValue(true);
            spojniceSessionRepository.delete(matchId);
        }
    }

    private void setupRoundData(Spojnice data) {
        pairMap.clear();
        for (int i = 0; i < data.getQuestions().size(); i++) {
            pairMap.put(data.getQuestions().get(i), data.getAnswers().get(i));
        }

        List<String> left = new ArrayList<>(data.getQuestions());
        List<String> right = new ArrayList<>(data.getAnswers());
        Collections.shuffle(left);
        Collections.shuffle(right);

        leftColumn.postValue(left);
        rightColumn.postValue(right);
        
        currentLeftIndex.postValue(0);
        player1Matches.postValue(new HashMap<>());
        player2Matches.postValue(new HashMap<>());
        missedMatches.postValue(new HashMap<>());
        p1RoundScore.postValue(0);
        p2RoundScore.postValue(0);
        updateSessionData();
    }

    private void startPlayerTurn(int player) {
        currentPlayerTurn.postValue(player);
        waitingForNextPlayer.postValue(false);
        
        if (player != startingPlayerOfRound.getValue()) {
            // Second player's turn - jump to first unmatched
            findNextUnmatched(0);
        } else {
            currentLeftIndex.postValue(0);
        }

        startTimer();
        updateSessionData();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timeLeft.postValue(SpojniceConfig.TIME_PER_PLAYER_SECONDS);
        timer = new CountDownTimer(SpojniceConfig.TIME_PER_PLAYER_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.postValue((int) (millisUntilFinished / 1000) + 1);
            }

            @Override
            public void onFinish() {
                timeLeft.postValue(0);
                handleTurnEnd();
            }
        }.start();
    }

    public void onRightTermSelected(int rightIndex) {
        Integer leftIdx = currentLeftIndex.getValue();
        if (leftIdx == null || leftIdx >= SpojniceConfig.TERMS_COUNT) return;

        List<String> leftList = leftColumn.getValue();
        List<String> rightList = rightColumn.getValue();
        if (leftList == null || rightList == null) return;

        String leftTerm = leftList.get(leftIdx);
        String rightTerm = rightList.get(rightIndex);

        boolean isCorrect = rightTerm.equals(pairMap.get(leftTerm));
        int currentPlayer = currentPlayerTurn.getValue();

        if (isCorrect) {
            if (currentPlayer == 1) {
                Map<Integer, Integer> matches = player1Matches.getValue();
                if (matches == null) matches = new HashMap<>();
                matches.put(leftIdx, rightIndex);
                player1Matches.setValue(matches);
                player1TotalScore += SpojniceConfig.POINTS_PER_MATCH;
                player1CorrectAccumulated++;
                p1ScoreDelta.setValue(SpojniceConfig.POINTS_PER_MATCH);
            } else {
                Map<Integer, Integer> matches = player2Matches.getValue();
                if (matches == null) matches = new HashMap<>();
                matches.put(leftIdx, rightIndex);
                player2Matches.setValue(matches);
                p2ScoreDelta.setValue(SpojniceConfig.POINTS_PER_MATCH);
            }
        } else if (currentPlayer != startingPlayerOfRound.getValue()) {
            // If second player misses, mark as missed immediately
            markAsMissed(leftIdx);
        }

        // Regardless of correct/incorrect, move to next in this player's run
        findNextUnmatched(leftIdx + 1);
        updateSessionData();
    }

    private void findNextUnmatched(int startIndex) {
        int nextIndex = startIndex;
        int startingPlayer = startingPlayerOfRound.getValue();
        int currentPlayer = currentPlayerTurn.getValue();

        Map<Integer, Integer> p1 = player1Matches.getValue();
        Map<Integer, Integer> p2 = player2Matches.getValue();
        Map<Integer, Integer> missed = missedMatches.getValue();

        while (nextIndex < SpojniceConfig.TERMS_COUNT && 
               ((p1 != null && p1.containsKey(nextIndex)) || 
                (p2 != null && p2.containsKey(nextIndex)) ||
                (missed != null && missed.containsKey(nextIndex)))) {
            nextIndex++;
        }

        if (nextIndex < SpojniceConfig.TERMS_COUNT) {
            currentLeftIndex.setValue(nextIndex);
        } else {
            handleTurnEnd();
        }
    }

    private void handleTurnEnd() {
        if (timer != null) timer.cancel();
        
        int currentPlayer = currentPlayerTurn.getValue();
        int startingPlayer = startingPlayerOfRound.getValue();

        if (currentPlayer == startingPlayer) {
            // Check if there's anything left for the second player
            if (hasUnmatchedTerms()) {
                waitingForNextPlayer.setValue(true);
                updateSessionData();
            } else {
                endRound();
            }
        } else {
            // Second player finished, mark any remaining as missed
            revealRemainingAsMissed();
            endRound();
        }
    }

    private boolean hasUnmatchedTerms() {
        Map<Integer, Integer> p1 = player1Matches.getValue();
        Map<Integer, Integer> p2 = player2Matches.getValue();
        for (int i = 0; i < SpojniceConfig.TERMS_COUNT; i++) {
            if ((p1 == null || !p1.containsKey(i)) && (p2 == null || !p2.containsKey(i))) {
                return true;
            }
        }
        return false;
    }

    private void revealRemainingAsMissed() {
        Map<Integer, Integer> p1 = player1Matches.getValue();
        Map<Integer, Integer> p2 = player2Matches.getValue();
        Map<Integer, Integer> missed = missedMatches.getValue();
        if (missed == null) missed = new HashMap<>();

        for (int i = 0; i < SpojniceConfig.TERMS_COUNT; i++) {
            if ((p1 == null || !p1.containsKey(i)) && (p2 == null || !p2.containsKey(i)) && !missed.containsKey(i)) {
                markAsMissed(i);
            }
        }
    }

    private void markAsMissed(int leftIdx) {
        Map<Integer, Integer> missed = missedMatches.getValue();
        if (missed == null) missed = new HashMap<>();
        
        List<String> leftList = leftColumn.getValue();
        List<String> rightList = rightColumn.getValue();
        if (leftList != null && rightList != null) {
            String correctRightTerm = pairMap.get(leftList.get(leftIdx));
            int correctRightIdx = rightList.indexOf(correctRightTerm);
            missed.put(leftIdx, correctRightIdx);
            missedMatches.setValue(missed);
        }
    }

    private void endRound() {
        if (currentRound.getValue() < SpojniceConfig.ROUNDS_COUNT) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startRound(currentRound.getValue() + 1, allSpojnice.getValue());
            }, 2000);
            updateSessionData();
        } else {
            p1ScoreDelta.postValue(0);
            p2ScoreDelta.postValue(0);
            gameFinished.postValue(true);
            spojniceSessionRepository.delete(matchId);
            updateUserStatistics();
        }
    }

    private boolean statsUpdated = false;

    private void updateUserStatistics() {
        if (statsUpdated) return;
        statsUpdated = true;
        String userId = getCurrentUserId();
        statsRepository.getStatistics(userId).thenAccept(stats -> {
            UserStatistics finalStats = (stats != null) ? stats : UserStatistics.createNew(userId);
            
            // Accuracy Update: Each pair in Spojnice is a 'question'
            // Total questions = ROUNDS_COUNT * TERMS_COUNT
            long gameTotal = (long) SpojniceConfig.TERMS_COUNT * SpojniceConfig.ROUNDS_COUNT;
            int gameCorrect = player1CorrectAccumulated;
            double oldAccuracy = finalStats.getSpojnice();

            long newTotal = finalStats.getSpojniceTotal() + gameTotal;
            long newCorrect = finalStats.getSpojniceCorrect() + gameCorrect;
            finalStats.setSpojniceTotal(newTotal);
            finalStats.setSpojniceCorrect(newCorrect);
            if (newTotal > 0) {
                finalStats.setSpojnice((double) newCorrect / newTotal * 100.0);
            }
            double newAccuracy = finalStats.getSpojnice();

            // Track points and game count
            finalStats.setSpojnicePoints(finalStats.getSpojnicePoints() + player1TotalScore);
            finalStats.setSpojnicePlayed(finalStats.getSpojnicePlayed() + 1);

            Log.d("UserStats", String.format("Stats changed: Spojnice - Game Correct: %d/%d | Total Accuracy: %.1f%% -> %.1f%%",
                    gameCorrect, (int)gameTotal, oldAccuracy, newAccuracy));
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

    public void startSecondPlayerTurn() {
        startPlayerTurn(startingPlayerOfRound.getValue() == 1 ? 2 : 1);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
    }

    public void updateSessionData(List<Spojnice> spojniceList){
        if (spojniceList == null || spojniceList.size() < 2 || matchId == null) return;
        SpojniceSessionData data = new SpojniceSessionData(
                currentRound.getValue(),
                currentPlayerTurn.getValue(),
                Boolean.TRUE.equals(isGameFinished().getValue()),
                spojniceList.get(0).getSpojniceId(),
                spojniceList.get(1).getSpojniceId(),
                startingPlayerOfRound.getValue(),
                currentLeftIndex.getValue(),
                toStringKeyMap(player1Matches.getValue()),
                toStringKeyMap(player2Matches.getValue()),
                toStringKeyMap(missedMatches.getValue()),
                Boolean.TRUE.equals(waitingForNextPlayer.getValue())
                );
        spojniceSessionRepository.updateSessionData(matchId, data);
    }

    private Map<String, Integer> toStringKeyMap(Map<Integer, Integer> map) {
        if (map == null) return new HashMap<>();
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    // Convenience overload for when allSpojnice is already set
    public void updateSessionData() {
        updateSessionData(allSpojnice.getValue());
    }
}
