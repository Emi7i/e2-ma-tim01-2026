package com.example.slagalica.presentation.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.match.Match;
import com.example.slagalica.domain.model.match.MatchSessionData;
import com.example.slagalica.domain.model.match.games.MatchType;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.service.match.KorakPoKorakService;
import com.example.slagalica.domain.service.match.MatchService;
import com.example.slagalica.domain.service.match.MojBrojService;
import com.example.slagalica.domain.service.progression.LeagueNotificationService;
import com.example.slagalica.repository.impl.RankingRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.google.firebase.firestore.auth.User;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

@HiltViewModel
public class MatchViewModel extends ViewModel {
    @Getter
    private Match match;

    private final MatchService matchService;
    private final KorakPoKorakService korakPoKorakService;
    private final MojBrojService mojBrojService;
    private final UserProfileRepository userProfileRepository;
    private final SessionManager sessionManager;
    private final RankingRepository rankingRepository;
    private final LeagueNotificationService leagueNotificationService;

//    private final MutableLiveData<IGame> currentGame = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentGameId = new MutableLiveData<>();

    @Inject
    public MatchViewModel(
            KorakPoKorakService korakPoKorakService,
            MojBrojService mojBrojService,
            UserProfileRepository userProfileRepository,
            MatchService matchService,
            SessionManager sessionManager,
            LeagueNotificationService leagueNotificationService
            SessionManager sessionManager,
            RankingRepository rankingRepository
    ) {
        this.matchService = matchService;
        this.korakPoKorakService = korakPoKorakService;
        this.mojBrojService = mojBrojService;
        this.userProfileRepository = userProfileRepository;
        this.sessionManager = sessionManager;
        this.leagueNotificationService = leagueNotificationService;
        this.rankingRepository = rankingRepository;
    }

    public void startMatch(String player1Id, String player2Id, MatchType matchType) {
        Log.d("Match", "Starting match...");
        CompletableFuture<UserProfile> player1Future = userProfileRepository.getProfile(player1Id);
        CompletableFuture<UserProfile> player2Future = userProfileRepository.getProfile(player2Id);

        CompletableFuture.allOf(player1Future, player2Future)
                .thenAccept(ignored -> {
                    Log.d("Match", "Fetching players...");

                    UserProfile player1 = player1Future.join();
                    UserProfile player2 = player2Future.join();

                    Log.d("Match", "Player1: " + player1);
                    Log.d("Match", "Player2: " + player2);

                    if (player1 == null || player2 == null) {
                        Log.e("Match", "One or both profiles are null!");
                        return;
                    }

                    if (matchType == MatchType.CLASSIC) {
                        deductToken(sessionManager.getCurrentUserId())
                                .thenAccept(success -> {
                                    Log.d("Match", "deductToken result: " + success);
                                    if (success) createMatch(player1, player2, matchType);
                                })
                                .exceptionally(throwable -> {
                                    Log.e("Match", "deductToken failed", throwable);
                                    return null;
                                });
                    } else {
                        // other logic for other types if needed
                        createMatch(player1, player2, matchType);
                    }
                })
                .exceptionally(throwable -> {
                    Log.e("Match", "Error starting match", throwable);
                    return null;
                });
    }

    private final MutableLiveData<Boolean> isGameActive = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> player1Score = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> player2Score = new MutableLiveData<>(0);
    private final MutableLiveData<String> player1Name = new MutableLiveData<>("Igrač 1");
    private final MutableLiveData<String> player2Name = new MutableLiveData<>("Igrač 2");
    private final MutableLiveData<String> activePlayer = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> insufficientTokens = new MutableLiveData<>();
    public LiveData<Boolean> getInsufficientTokens() { return insufficientTokens; }

    public LiveData<Boolean> getIsGameActive() { return isGameActive; }
    public LiveData<Integer> getPlayer1Score() { return player1Score; }
    public LiveData<Integer> getPlayer2Score() { return player2Score; }
    public LiveData<String> getPlayer1Name() { return player1Name; }
    public LiveData<String> getPlayer2Name() { return player2Name; }
    public LiveData<String> getActivePlayer() { return activePlayer; }

    public void setGameActive(boolean active) {
        isGameActive.setValue(active);
        Log.d("Match", "Game active: " + isGameActive.getValue());
    }

    public LiveData<Integer> getCurrentGameId() { return currentGameId; }

    public void setPlayerNames(String name1, String name2) {
        player1Name.setValue(name1);
        player2Name.setValue(name2);
    }

    public void setActivePlayer(String playerId) {
        activePlayer.setValue(playerId);
    }

    public void setActivePlayer(int playerNum){
        String active;
        if(playerNum == 1){
            active = match.getPlayer1Id();
        }
        else{
            active = match.getPlayer2Id();
        }
        match.setActivePlayer(active);
        match.updateMatchSession();
        onMatchUpdated(match);
    }

    public void setPlayer1Score(int score) {
        match.setPlayer1Score(score);
        match.updateMatchSession();
        onMatchUpdated(match);
    }

    public void setPlayer2Score(int score) {
        match.setPlayer2Score(score);
        match.updateMatchSession();
        onMatchUpdated(match);
    }

    private void onMatchUpdated(Match match){
        player1Score.postValue(match.getPlayer1Score());
        player2Score.postValue(match.getPlayer2Score());
        activePlayer.postValue(match.getActivePlayer());
        currentGameId.postValue(match.getCurrentGameId());
    }

    private CompletableFuture<Boolean> deductToken(String playerId) {
        return userProfileRepository.getProfile(playerId)
                .thenApply(player -> {
                    if (player == null) return false;
                    if (player.getNumTokens() >= 1) {
                        player.setNumTokens(player.getNumTokens() - 1);
                        userProfileRepository.saveProfile(player);
                        sessionManager.setCurrentProfile(player);
                        insufficientTokens.postValue(false);
                        return true;
                    } else {
                        Log.d("Match", "PLAYER IS POOR");
                        insufficientTokens.postValue(true);
                        return false;
                    }
                });
    }

    private void createMatch(UserProfile player1, UserProfile player2, MatchType matchType){
        MatchSessionData data = new MatchSessionData();
        data.player1Id = player1.getUserId();
        data.player2Id = player2.getUserId();

        Log.d("Match", "Players fetched");

        match = new Match(
                player1.getUserId(),
                player2.getUserId(),
                0,
                0,
                player1.getUsername(),
                player2.getUsername(),
                matchType,
                matchService,
                korakPoKorakService,
                mojBrojService,
                userProfileRepository,
                rankingRepository,
                sessionManager,
                leagueNotificationService,
                () -> {
                    isGameActive.postValue(true);
                    match.setOnMatchUpdatedListener(this::onMatchUpdated);
                    match.start();
                }
        );
    }
}
