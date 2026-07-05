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
import com.example.slagalica.domain.model.tournament.TournamentMatch;
import com.example.slagalica.domain.model.tournament.TournamentResultUi;
import com.example.slagalica.domain.model.tournament.TournamentRound;
import com.example.slagalica.domain.service.match.KorakPoKorakService;
import com.example.slagalica.domain.service.match.MatchService;
import com.example.slagalica.domain.service.match.MojBrojService;
import com.example.slagalica.repository.impl.RankingRepository;
import com.example.slagalica.repository.impl.TournamentRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.google.firebase.firestore.auth.User;

import java.util.Objects;
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
    private final TournamentRepository tournamentRepository;

//    private final MutableLiveData<IGame> currentGame = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentGameId = new MutableLiveData<>();

    @Inject
    public MatchViewModel(
            KorakPoKorakService korakPoKorakService,
            MojBrojService mojBrojService,
            UserProfileRepository userProfileRepository,
            MatchService matchService,
            SessionManager sessionManager,
            RankingRepository rankingRepository,
            TournamentRepository tournamentRepository
    ) {
        this.matchService = matchService;
        this.korakPoKorakService = korakPoKorakService;
        this.mojBrojService = mojBrojService;
        this.userProfileRepository = userProfileRepository;
        this.sessionManager = sessionManager;
        this.rankingRepository = rankingRepository;
        this.tournamentRepository = tournamentRepository;
    }

    public void startMatch(String player1Id, String player2Id, MatchType matchType) {
        Log.d("Match", "Starting match...");
        MatchType safeMatchType = matchType == null ? MatchType.CLASSIC : matchType;

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

                    if (safeMatchType == MatchType.CLASSIC) {     //da bi classic skinuo 1token, a ostali tipovi ne skidaju
                        deductToken(sessionManager.getCurrentUserId())
                                .thenAccept(success -> {
                                    if (success) createMatch(player1, player2, safeMatchType);
                                });
                    } else {
                        // other logic for other types if needed
                        createMatch(player1, player2, safeMatchType);
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
    private final MutableLiveData<TournamentResultUi> tournamentResult = new MutableLiveData<>();
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

    public LiveData<TournamentResultUi> getTournamentResult() { return tournamentResult; }
    public void clearTournamentResult() { tournamentResult.setValue(null); }

    private void onMatchUpdated(Match match){
        if(match == null) { return; }
        player1Score.postValue(match.getPlayer1Score());
        player2Score.postValue(match.getPlayer2Score());

        player1Name.postValue(safeName(match.getPlayer1Name(), "Igrač 1"));
        player2Name.postValue(safeName(match.getPlayer2Name(), "Igrač 2"));

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
                        return true;
                    } else {
                        Log.d("Match", "PLAYER IS POOR");
                        insufficientTokens.postValue(true);
                        return false;
                    }
                });
    }

    private boolean isTournamentType(MatchType matchType) {
        return matchType == MatchType.TOURNAMENT_SEMIFINAL
                || matchType == MatchType.TOURNAMENT_FINAL;
    }

    private TournamentResultUi buildTournamentResult(Match finishedMatch) {
        if (finishedMatch == null) {
            return null;
        }

        String currentUserId = sessionManager.getCurrentUserId();

        boolean isFinal = finishedMatch.getMatchType() == MatchType.TOURNAMENT_FINAL;

        String winnerId = finishedMatch.getPlayer1Score() >= finishedMatch.getPlayer2Score()
                ? finishedMatch.getPlayer1Id()
                : finishedMatch.getPlayer2Id();

        boolean currentUserWon = Objects.equals(currentUserId, winnerId);

        int myScore = Objects.equals(currentUserId, finishedMatch.getPlayer1Id())
                ? finishedMatch.getPlayer1Score()
                : finishedMatch.getPlayer2Score();

        if (!isFinal) {
            if (currentUserWon) {
                long stars = 10L + myScore / 40L;

                return new TournamentResultUi(
                        "Pobedili ste u polufinalu!",
                        "Osvojili ste +2 tokena i +" + stars + " zvezdica. Plasirali ste se u finale.",
                        true
                );
            }

            return new TournamentResultUi(
                    "Izgubili ste polufinale",
                    "Izgubili ste. Ispali ste iz turnira.",
                    false
            );
        }

        if (currentUserWon) {
            long stars = 20L + myScore / 40L;

            return new TournamentResultUi(
                    "Pobedili ste u finalu!",
                    "Osvojili ste turnir! Dobijate +3 tokena i +" + stars + " zvezdica.",
                    true
            );
        }

        return new TournamentResultUi(
                "Izgubili ste finale",
                "Izgubili ste finale.",
                false
        );
    }

    private void prepareTournamentResultCallback(MatchType matchType) {
        if (match == null || !isTournamentType(matchType)) {
            return;
        }

        match.setOnTournamentResultResolvedListener(() -> {
            TournamentResultUi result = buildTournamentResult(match);
            if (result != null) {
                tournamentResult.postValue(result);
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
                () -> {
                    isGameActive.postValue(true);

                    player1Name.postValue(safeName(player1.getUsername(), "Igrač 1"));
                    player2Name.postValue(safeName(player2.getUsername(), "Igrač 2"));
                    player1Score.postValue(0);
                    player2Score.postValue(0);
                    activePlayer.postValue(player1.getUserId());

                    match.setOnMatchUpdatedListener(this::onMatchUpdated);
                    prepareTournamentResultCallback(matchType);

                    onMatchUpdated(match);

                    match.start();
                }
        );
    }

    private String safeName(String name, String fallback) {
        if (name == null || name.trim().isEmpty()) {
            return fallback;
        }
        return name;
    }

    public void startTournamentMatch(
            UserProfile player1,
            UserProfile player2,
            MatchType matchType,
            String tournamentId,
            String tournamentMatchId
    ) {
        createMatch(player1, player2, matchType);

        if (match != null) {
            match.attachTournament(
                    tournamentRepository,
                    tournamentId,
                    tournamentMatchId
            );
        }
    }

    public void startExistingTournamentMatch(String tournamentId, TournamentMatch tournamentMatch) {
        if (tournamentId == null || tournamentMatch == null) {
            Log.e("Match", "Tournament match is null.");
            return;
        }

        tournamentRepository.startTournamentMatch(tournamentId, tournamentMatch.getMatchId())
                .thenCompose(startedMatch -> {
                    if (startedMatch == null || startedMatch.getMatchSessionId() == null) {
                        throw new IllegalStateException("Match session nije kreiran.");
                    }

                    CompletableFuture<UserProfile> player1Future = userProfileRepository.getProfile(startedMatch.getPlayer1Id());

                    CompletableFuture<UserProfile> player2Future = userProfileRepository.getProfile(startedMatch.getPlayer2Id());

                    return CompletableFuture
                            .allOf(player1Future, player2Future)
                            .thenApply(ignored -> {
                                UserProfile player1 = player1Future.join();
                                UserProfile player2 = player2Future.join();

                                if (player1 == null || player2 == null) {
                                    throw new IllegalStateException("Profil jednog od igrača nije pronađen.");
                                }

                                MatchType matchType = startedMatch.getRoundEnum() == TournamentRound.FINAL
                                        ? MatchType.TOURNAMENT_FINAL
                                        : MatchType.TOURNAMENT_SEMIFINAL;

                                match = new Match(
                                        startedMatch.getMatchSessionId(),
                                        player1.getUserId(),
                                        player2.getUserId(),
                                        startedMatch.getPlayer1Score(),
                                        startedMatch.getPlayer2Score(),
                                        player1.getUsername(),
                                        player2.getUsername(),
                                        player1.getUserId(),
                                        1,
                                        matchType,
                                        matchService,
                                        korakPoKorakService,
                                        mojBrojService,
                                        userProfileRepository,
                                        rankingRepository,
                                        sessionManager,
                                        () -> {
                                            isGameActive.postValue(true);
                                        }
                                );

                                match.attachTournament(tournamentRepository, tournamentId, startedMatch.getMatchId());
                                match.setOnMatchUpdatedListener(this::onMatchUpdated);
                                prepareTournamentResultCallback(matchType);

                                currentGameId.postValue(1);
                                player1Name.postValue(safeName(player1.getUsername(), "Igrač 1"));
                                player2Name.postValue(safeName(player2.getUsername(), "Igrač 2"));
                                player1Score.postValue(startedMatch.getPlayer1Score());
                                player2Score.postValue(startedMatch.getPlayer2Score());
                                activePlayer.postValue(player1.getUserId());

                                onMatchUpdated(match);
                                return null;
                            });
                })
                .exceptionally(throwable -> {
                    Log.e("Match", "Failed to start tournament match", throwable);
                    return null;
                });
    }
}
