package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.domain.model.config.KoZnaZnaConfig;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class KoZnaZnaViewModel extends ViewModel {

    private final KoZnaZnaRepository repository;
    private final com.example.slagalica.repository.impl.UserStatisticsRepository statsRepository;
    private static final String MOCK_USER_ID = "test_user_123";
    
    private final MutableLiveData<List<KoZnaZna>> questions = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(-1);
    private final MutableLiveData<KoZnaZna> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<List<String>> currentAnswers = new MutableLiveData<>();
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> timeLeft = new MutableLiveData<>(KoZnaZnaConfig.TIME_PER_QUESTION_SECONDS);
    private final MutableLiveData<Boolean> gameFinished = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> player1Delta = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> player2Delta = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> canAnswer = new MutableLiveData<>(true);
    private final MutableLiveData<String> lastSelectedAnswer = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> currentPlayerTurn = new MutableLiveData<>(1); // 1 or 2
    private final MutableLiveData<Boolean> waitingForNextPlayer = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> revealingAnswer = new MutableLiveData<>(false);

    private final MutableLiveData<String> player1AnswerLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<String> player2AnswerLiveData = new MutableLiveData<>(null);

    private CountDownTimer timer;
    private long lastQuestionStartTime;
    
    private String player1Answer = null;
    private long player1Time = Long.MAX_VALUE;
    private int player1TotalScore = 0;
    private int player1CorrectCount = 0;
    private String player2Answer = null;
    private long player2Time = Long.MAX_VALUE;

    @Inject
    public KoZnaZnaViewModel(KoZnaZnaRepository repository, com.example.slagalica.repository.impl.UserStatisticsRepository statsRepository) {
        this.repository = repository;
        this.statsRepository = statsRepository;
        loadQuestions();
    }

    public LiveData<KoZnaZna> getCurrentQuestion() { return currentQuestion; }
    public LiveData<List<String>> getCurrentAnswers() { return currentAnswers; }
    public LiveData<Integer> getCurrentQuestionIndex() { return currentQuestionIndex; }
    public LiveData<Integer> getScore() { return score; }
    public LiveData<Integer> getTimeLeft() { return timeLeft; }
    public LiveData<Boolean> isGameFinished() { return gameFinished; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Integer> getPlayer1Delta() { return player1Delta; }
    public LiveData<Integer> getPlayer2Delta() { return player2Delta; }
    public LiveData<Boolean> getCanAnswer() { return canAnswer; }
    public LiveData<String> getLastSelectedAnswer() { return lastSelectedAnswer; }
    public LiveData<Integer> getCurrentPlayerTurn() { return currentPlayerTurn; }
    public LiveData<Boolean> isWaitingForNextPlayer() { return waitingForNextPlayer; }
    public LiveData<Boolean> isRevealingAnswer() { return revealingAnswer; }
    public LiveData<String> getPlayer1Answer() { return player1AnswerLiveData; }
    public LiveData<String> getPlayer2Answer() { return player2AnswerLiveData; }

    private void loadQuestions() {
        isLoading.setValue(true);
        repository.getRandomQuestions(KoZnaZnaConfig.QUESTIONS_COUNT).thenAccept(loadedQuestions -> {
            if (loadedQuestions == null || loadedQuestions.isEmpty()) {
                android.util.Log.e("KoZnaZnaViewModel", "No questions found in Firestore!");
                isLoading.postValue(false);
                return;
            }
            
            // Shuffle all available questions
            java.util.Collections.shuffle(loadedQuestions);
            
            // Limit to QUESTIONS_COUNT defined in config
            final List<KoZnaZna> finalQuestions = loadedQuestions.subList(0, Math.min(KoZnaZnaConfig.QUESTIONS_COUNT, loadedQuestions.size()));
            
            android.util.Log.d("KoZnaZnaViewModel", "Starting game with " + finalQuestions.size() + " shuffled questions");
            questions.postValue(finalQuestions);
            isLoading.postValue(false);
            
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> startWithQuestions(finalQuestions));
        }).exceptionally(ex -> {
            android.util.Log.e("KoZnaZnaViewModel", "Error loading questions", ex);
            isLoading.postValue(false);
            return null;
        });
    }

    private void startWithQuestions(List<KoZnaZna> loadedQuestions) {
        currentQuestionIndex.setValue(-1);
        nextQuestionInternal(loadedQuestions);
    }

    public void nextQuestion() {
        List<KoZnaZna> questionList = questions.getValue();
        nextQuestionInternal(questionList);
    }

    private void nextQuestionInternal(List<KoZnaZna> questionList) {
        Integer index = currentQuestionIndex.getValue();
        if (index == null) index = -1;
        index++;
        
        if (questionList != null && index < questionList.size()) {
            player1Answer = null;
            player1Time = Long.MAX_VALUE;
            player2Answer = null;
            player2Time = Long.MAX_VALUE;
            revealingAnswer.setValue(false);
            waitingForNextPlayer.setValue(false);
            currentPlayerTurn.setValue(1);
            
            canAnswer.setValue(true);
            lastSelectedAnswer.setValue(null);
            player1Delta.setValue(0);
            player2Delta.setValue(0);
            currentQuestionIndex.setValue(index);
            KoZnaZna q = questionList.get(index);
            currentQuestion.postValue(q);
            
            List<String> allAnswers = new ArrayList<>(q.getOtherAnswers());
            allAnswers.add(q.getCorrectAnswer());
            Collections.shuffle(allAnswers);
            currentAnswers.postValue(allAnswers);
            
            startPlayerTurn(1);
        } else if (questionList != null) {
            gameFinished.postValue(true);
            updateUserStatistics();
        }
    }

    private void updateUserStatistics() {
        statsRepository.getStatistics(MOCK_USER_ID).thenAccept(stats -> {
            if (stats != null) {
                stats.setGamesPlayed(stats.getGamesPlayed() + 1);
                
                // Accuracy Update
                long newTotal = stats.getKoZnaZnaTotal() + (questions.getValue() != null ? questions.getValue().size() : 0);
                long newCorrect = stats.getKoZnaZnaCorrect() + player1CorrectCount;
                stats.setKoZnaZnaTotal(newTotal);
                stats.setKoZnaZnaCorrect(newCorrect);
                if (newTotal > 0) {
                    stats.setKoZnaZna((double) newCorrect / newTotal * 100.0);
                }
                
                stats.calculateOverallStats();
                
                if (player1TotalScore > 0) {
                    stats.setWonGames(stats.getWonGames() + 1);
                }
                statsRepository.saveStatistics(stats);
            }
        });
    }

    private void startPlayerTurn(int player) {
        currentPlayerTurn.postValue(player);
        waitingForNextPlayer.postValue(false);
        canAnswer.postValue(true);
        lastSelectedAnswer.postValue(null);
        lastQuestionStartTime = System.currentTimeMillis();
        startTimer();
    }

    public void startNextPlayerTurn() {
        startPlayerTurn(2);
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timeLeft.postValue(KoZnaZnaConfig.TIME_PER_QUESTION_SECONDS);
        timer = new CountDownTimer(KoZnaZnaConfig.TIME_PER_QUESTION_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.postValue((int) (millisUntilFinished / 1000) + 1);
            }

            @Override
            public void onFinish() {
                timeLeft.postValue(0);
                submitAnswer(null); // Time out
            }
        }.start();
    }

    public void submitAnswer(String answer) {
        if (Boolean.FALSE.equals(canAnswer.getValue())) return;
        canAnswer.postValue(false);
        lastSelectedAnswer.postValue(answer);
        
        if (timer != null) {
            timer.cancel();
        }

        long timeTaken = System.currentTimeMillis() - lastQuestionStartTime;
        
        if (currentPlayerTurn.getValue() == 1) {
            player1Answer = answer;
            player1Time = timeTaken;
            waitingForNextPlayer.setValue(true);
        } else {
            player2Answer = answer;
            player2Time = timeTaken;
            calculateResults();
        }
    }

    private void calculateResults() {
        KoZnaZna q = currentQuestion.getValue();
        if (q != null) {
            String correct = q.getCorrectAnswer();
            boolean p1Correct = correct.equalsIgnoreCase(player1Answer);
            if (p1Correct) player1CorrectCount++;
            boolean p2Correct = correct.equalsIgnoreCase(player2Answer);
            
            int p1D = 0;
            int p2D = 0;

            if (p1Correct) p1D = KoZnaZnaConfig.CORRECT_ANSWER_POINTS;
            else if (player1Answer != null) p1D = KoZnaZnaConfig.INCORRECT_ANSWER_POINTS;

            if (p2Correct) p2D = KoZnaZnaConfig.CORRECT_ANSWER_POINTS;
            else if (player2Answer != null) p2D = KoZnaZnaConfig.INCORRECT_ANSWER_POINTS;

            player1Delta.setValue(p1D);
            player2Delta.setValue(p2D);
            player1AnswerLiveData.setValue(player1Answer);
            player2AnswerLiveData.setValue(player2Answer);
            
            player1TotalScore += p1D;
            score.setValue(player1TotalScore);
        }
        
        revealingAnswer.setValue(true);
        // Delay before moving to next question
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::nextQuestion, 2000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
        }
    }
}
