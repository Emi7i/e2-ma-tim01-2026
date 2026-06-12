package com.example.slagalica.presentation.viewmodels;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.slagalica.domain.model.match.games.KoZnaZna;
import com.example.slagalica.domain.model.match.games.KoZnaZnaConfig;
import com.example.slagalica.repository.impl.KoZnaZnaRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class KoZnaZnaViewModel extends ViewModel {

    private final KoZnaZnaRepository repository;
    
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

    private CountDownTimer timer;
    private long lastQuestionStartTime;

    @Inject
    public KoZnaZnaViewModel(KoZnaZnaRepository repository) {
        this.repository = repository;
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

    private void loadQuestions() {
        isLoading.setValue(true);
        repository.getRandomQuestions(KoZnaZnaConfig.QUESTIONS_COUNT).thenAccept(loadedQuestions -> {
            if (loadedQuestions == null || loadedQuestions.isEmpty()) {
                android.util.Log.w("KoZnaZnaViewModel", "No questions in Firestore, seeding data...");
                List<KoZnaZna> demoQuestions = new com.example.slagalica.domain.service.match.KoZnaZnaDemoFactory().createDemoQuestions();
                repository.seedData(demoQuestions).thenAccept(v -> {
                    android.util.Log.d("KoZnaZnaViewModel", "Data seeded successfully");
                    // After seeding, we can just use the demo questions immediately for this session
                    questions.postValue(demoQuestions);
                    isLoading.postValue(false);
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> startWithQuestions(demoQuestions));
                }).exceptionally(ex -> {
                    android.util.Log.e("KoZnaZnaViewModel", "Error seeding data", ex);
                    // Still use demo questions in memory so game can start
                    questions.postValue(demoQuestions);
                    isLoading.postValue(false);
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> startWithQuestions(demoQuestions));
                    return null;
                });
            } else {
                android.util.Log.d("KoZnaZnaViewModel", "Loaded questions from Firestore: " + loadedQuestions.size());
                questions.postValue(loadedQuestions);
                isLoading.postValue(false);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> startWithQuestions(loadedQuestions));
            }
        }).exceptionally(ex -> {
            android.util.Log.e("KoZnaZnaViewModel", "Error loading questions", ex);
            isLoading.postValue(false);
            // Fallback to demo questions in memory
            List<KoZnaZna> demoQuestions = new com.example.slagalica.domain.service.match.KoZnaZnaDemoFactory().createDemoQuestions();
            questions.postValue(demoQuestions);
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> startWithQuestions(demoQuestions));
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
            canAnswer.postValue(true);
            lastSelectedAnswer.postValue(null);
            player1Delta.postValue(0);
            player2Delta.postValue(0);
            currentQuestionIndex.postValue(index);
            KoZnaZna q = questionList.get(index);
            currentQuestion.postValue(q);
            
            List<String> allAnswers = new ArrayList<>(q.getOtherAnswers());
            allAnswers.add(q.getCorrectAnswer());
            Collections.shuffle(allAnswers);
            currentAnswers.postValue(allAnswers);
            
            lastQuestionStartTime = System.currentTimeMillis();
            startTimer();
        } else if (questionList != null) {
            gameFinished.postValue(true);
        }
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
                handleNoAnswer();
            }
        }.start();
    }

    private void handleNoAnswer() {
        canAnswer.postValue(false);
        // Simulate opponent in case of no answer? 
        // For simplicity, just move next.
        nextQuestion();
    }

    public void submitAnswer(String answer) {
        if (Boolean.FALSE.equals(canAnswer.getValue())) return;
        canAnswer.postValue(false);
        lastSelectedAnswer.postValue(answer);
        
        if (timer != null) {
            timer.cancel();
        }
        
        long timeOfSubmission = System.currentTimeMillis();
        // Simulate opponent: 50% chance they got it right, random time
        boolean opponentCorrect = new java.util.Random().nextBoolean();
        long opponentTimeOfSubmission = lastQuestionStartTime + (new java.util.Random().nextInt(4000) + 500);

        KoZnaZna q = currentQuestion.getValue();
        if (q != null) {
            boolean playerCorrect = q.getCorrectAnswer().equals(answer);
            int p1DeltaValue = 0;
            int p2DeltaValue = 0;

            if (playerCorrect) {
                if (opponentCorrect) {
                    if (timeOfSubmission < opponentTimeOfSubmission) {
                        p1DeltaValue = KoZnaZnaConfig.CORRECT_ANSWER_POINTS;
                    } else {
                        p2DeltaValue = KoZnaZnaConfig.CORRECT_ANSWER_POINTS;
                    }
                } else {
                    p1DeltaValue = KoZnaZnaConfig.CORRECT_ANSWER_POINTS;
                }
            } else {
                p1DeltaValue = KoZnaZnaConfig.INCORRECT_ANSWER_POINTS;
                if (opponentCorrect) {
                    p2DeltaValue = KoZnaZnaConfig.CORRECT_ANSWER_POINTS;
                }
            }

            player1Delta.postValue(p1DeltaValue);
            player2Delta.postValue(p2DeltaValue);
            
            int currentScore = score.getValue() != null ? score.getValue() : 0;
            score.postValue(currentScore + p1DeltaValue);
        }
        
        // Short delay before next question
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::nextQuestion, 1000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
        }
    }
}
