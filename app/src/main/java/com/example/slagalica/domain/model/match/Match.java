package com.example.slagalica.domain.model.match;

import android.util.Log;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.match.games.MatchType;
import com.example.slagalica.domain.model.match.games.common.GameSession;
import com.example.slagalica.domain.model.match.games.common.IGame;
import com.example.slagalica.domain.model.match.games.common.OnMatchUpdatedListener;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorak;
import com.example.slagalica.domain.model.match.games.mojbroj.MojBroj;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.progression.League;
import com.example.slagalica.domain.service.progression.LeagueNotificationService;
import com.example.slagalica.domain.service.match.KorakPoKorakService;
import com.example.slagalica.domain.service.match.MatchService;
import com.example.slagalica.domain.service.match.MojBrojService;
import com.example.slagalica.repository.impl.RankingRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Match {
    private String id;
    private String player1Id;
    private String player2Id;
    private int player1Score;
    private int player2Score;
    private String player1Name;
    private String player2Name;
    private String activePlayer;
    private int currentGameId;
    private IGame currentGame;

    private MatchType matchType;

    private final MatchService matchService;
    private final KorakPoKorakService korakPoKorakService;
    private final MojBrojService mojBrojService;
    private final UserProfileRepository userProfileRepository;
    private final RankingRepository rankingRepository;
    private final SessionManager sessionManager;
    private final LeagueNotificationService leagueNotificationService;

    private OnMatchUpdatedListener onMatchUpdatedListener;

    public Match(
                String matchId,
                 String player1Id,
                 String player2Id,
                 int player1Score,
                 int player2Score,
                 String player1Name,
                 String player2Name,
                 MatchType matchType,
                 MatchService matchService,
                 KorakPoKorakService korakPoKorakService,
                 MojBrojService mojBrojService,
                 UserProfileRepository userProfileRepository,
                 RankingRepository rankingRepository,
                 SessionManager sessionManager,
                 LeagueNotificationService leagueNotificationService,
                 Runnable onReadyCallback){
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.activePlayer = player1Id;
        this.matchType = matchType;
        this.matchService = matchService;
        this.korakPoKorakService = korakPoKorakService;
        this.mojBrojService = mojBrojService;
        this.userProfileRepository = userProfileRepository;
        this.rankingRepository = rankingRepository;
        this.sessionManager = sessionManager;
        this.leagueNotificationService = leagueNotificationService;
        this.currentGameId = 1;
        this.id = matchId;

        MatchSessionData data = new MatchSessionData(
                matchId,
                player1Id,
                player2Id,
                player1Score,
                player2Score,
                this.currentGameId,
                player1Id
        );

        this.matchService.update(matchId, data)
                .thenAccept(unused -> {
                    Log.d("Match", "Match created with existing id: " + matchId);
                    matchService.observe(matchId, this::handleRemoteMatchEvent);
                    if (onReadyCallback != null) onReadyCallback.run();
                })
                .exceptionally(throwable -> {
                    Log.d("Match", "Error creating match  / run onReadyCallback");
                    matchService.observe(matchId, this::onRemoteMatchUpdated);
                    return null;
                });
    }

    // for joining existing match
    public Match(String matchId,
                 MatchSessionData existingData,
                 MatchType matchType,
                 MatchService matchService,
                 KorakPoKorakService korakPoKorakService,
                 MojBrojService mojBrojService,
                 UserProfileRepository userProfileRepository,
                 SessionManager sessionManager,
                 RankingRepository rankingRepository,
                 LeagueNotificationService leagueNotificationService,
                 Runnable onReadyCallback) {
        this.id = matchId;
        this.player1Id = existingData.getPlayer1Id();
        this.player2Id = existingData.getPlayer2Id();
        this.player1Score = existingData.getPlayer1Score();
        this.player2Score = existingData.getPlayer2Score();
        this.activePlayer = existingData.getActivePlayer();
        this.currentGameId = existingData.getCurrentGameId();
        this.matchType = matchType;
        this.matchService = matchService;
        this.korakPoKorakService = korakPoKorakService;
        this.mojBrojService = mojBrojService;
        this.userProfileRepository = userProfileRepository;
        this.sessionManager = sessionManager;

        this.rankingRepository = rankingRepository;
        this.leagueNotificationService = leagueNotificationService;



        Log.d("Match", "Joined existing match: " + matchId);
        matchService.observe(matchId, this::handleRemoteMatchEvent);
        if (onReadyCallback != null)
            new android.os.Handler(android.os.Looper.getMainLooper()).post(onReadyCallback);
    }

    public void startNextGame(){
//        if (!isMyTurn()) return;
        switch(currentGameId){
            case 1:
                startSpojnice();
                break;
            case 2:
                startAsocijacije();
                break;
            case 3:
                startSkocko();
                break;
            case 4:
                startKorakPoKorak();
                break;
            case 5:
                startMojBroj();
                break;
            case 6:
                endMatch();
                break;
        }
    }

    public void start(){
         startForTesting(); // uncomment to instantly end the match with player 1 having 1 point (for testing win/loss stats)
//         startMojBroj(); // for fast testing!
//        startKoZnaZna();
    }

    private void startForTesting(){
        player1Score = 1;
        player2Score = 0;
        endMatch();
    }

    public void endMatch(){
        Log.d("Match", "ENDING MATCH ON THIS DEVICE");
        CompletableFuture<Void> completion;

//        if(matchType == MatchType.CLASSIC){  // Za klasicnu partiju obracunavaju se zvezde i rang lista.
//            completion = resolveClassicRewards();
//        } else {
//            completion = CompletableFuture.completedFuture(null);   //za ostale tipove se ne obradjuju nagrade
//        }
        // Note: Win/rate statistics are calculated in Moj Broj
        currentGameId = 0; // signal game over
        updateMatchSession();

        matchService.delete(id)
                    .exceptionally(error -> {
                        Log.e(
                                "Match",
                                "Failed to delete match session",
                                error
                        );
                        return null;
                    });
//        completion.whenComplete((ignored, throwable) -> {    // Match sesija se brise tek nakon zavrsene obrade nagrada.
//            if (throwable != null) {
//                Log.e(
//                        "Match",
//                        "Failed to resolve match rewards",
//                        throwable
//                );
//            }
//
//            matchService.delete(id)
//                    .exceptionally(error -> {
//                        Log.e(
//                                "Match",
//                                "Failed to delete match session",
//                                error
//                        );
//                        return null;
//                    });
//        });

        Log.d("Match", "Match ended!");
    }

    // Resolves rewards for classic match.
    // Add other methods for other scenarios,
    // for example tournament? or do that elsewhere
    //
    // Only ever reads/writes the logged-in user's own profile — a client isn't
    // allowed to write the opponent's document, so each participant's own client
    // is responsible for crediting/penalizing itself when the match ends.
    private CompletableFuture<Void> resolveClassicRewards(){
        Log.d("Match", "Resolving rewards...");
        String myId = sessionManager.getCurrentUserId();
        boolean iAmPlayer1 = Objects.equals(player1Id, myId);
        boolean iAmPlayer2 = Objects.equals(player2Id, myId);
        if (!iAmPlayer1 && !iAmPlayer2) {
            return CompletableFuture.completedFuture(null);
        }
        // player 1 is winner even if they have the same points >:D
        String winnerId = player1Score >= player2Score ? player1Id : player2Id;
        int myScore = iAmPlayer1 ? player1Score : player2Score;
        boolean iWon = Objects.equals(myId, winnerId);

        return userProfileRepository.getProfile(myId)
                .thenAccept(me -> {
                    if (me == null) return;

                    long starsBefore = me.getNumStars();
                    League oldLeague = League.fromDisplayName(me.getLeague());

                    // stars per 40 points
                    int additionalStars = myScore / 40;
                    me.setNumStars(me.getNumStars() + additionalStars);
                    me.setMonthlyStars(me.getMonthlyStars() + additionalStars);

                    // winner/loser stars
                    if (iWon) {
                        me.setNumStars(me.getNumStars() + 10);
                        me.setMonthlyStars(me.getMonthlyStars() + 10);
                    } else {
                        me.setNumStars(Math.max(0, me.getNumStars() - 10));
                        me.setMonthlyStars(Math.max(0, me.getMonthlyStars() - 10));
                    }

                    Log.d("Match", "Reward: iWon=" + iWon + " myScore=" + myScore
                            + " stars " + starsBefore + " -> " + me.getNumStars());

                    // Player automatically enters/leaves a league the moment their
                    // star total crosses a threshold, in either direction.
                    League newLeague = League.fromStars(me.getNumStars());
                    me.setLeague(newLeague.getDisplayName());

                    userProfileRepository.saveProfile(me)
                            .exceptionally(e -> { Log.e("Match", "Failed to save rewards", e); return null; });
                    sessionManager.setCurrentProfile(me);

                    if (newLeague != oldLeague) {
                        leagueNotificationService.notifyChange(myId, newLeague, newLeague.ordinal() > oldLeague.ordinal());
                    }
                })
                .exceptionally(throwable -> {
                    Log.e("Match", "Error fetching profile", throwable);
                    return null;
                });
    }

    public void startKoZnaZna(){
        currentGameId = 1;
        updateMatchSession();
    }

    public void startSpojnice(){
        currentGameId = 2;
        updateMatchSession();
    }

    public void startAsocijacije(){
        currentGameId = 3;
        updateMatchSession();
    }

    public void startSkocko(){
        currentGameId = 4;
        updateMatchSession();
    }

    public void startKorakPoKorak() {
        GameSession session = new GameSession(id, player1Id, player2Id);
        KorakPoKorak game = new KorakPoKorak(session, korakPoKorakService, sessionManager);
        game.setOnActivePlayerChangedListener(this::onActivePlayerChanged);
        game.setOnPointsChangedListener(this::onPointsChanged);
        game.setOnGameEndedListener(() -> {
            if (currentGame == game) { // ignore stale callbacks from a superseded game instance
                startNextGame();
            }
        });
//        game.getGameService().observeSessionData(id, game::onRemoteSessionUpdated);
        currentGameId = game.getId();
        currentGame = game;
        updateMatchSession();
    }

    public void startMojBroj() {
        GameSession session = new GameSession(id, player1Id, player2Id);
        MojBroj game = new MojBroj(session, mojBrojService);
        game.setOnActivePlayerChangedListener(this::onActivePlayerChanged);
        game.setOnPointsChangedListener(this::onPointsChanged);
        game.setOnGameEndedListener(() -> {
            if (currentGame == game) { // ignore stale callbacks from a superseded game instance
                startNextGame();
            }
        });
        currentGameId = game.getId();
        currentGame = game;
        updateMatchSession();
    }

    public void updatePlayer1Score(int delta) {
        Integer current = player1Score;
        player1Score = (current != null ? current : 0) + delta;
        updateMatchSession();
    }

    public void updatePlayer2Score(int delta) {
        Integer current = player2Score;
        player2Score = (current != null ? current : 0) + delta;
        updateMatchSession();
    }

    private void onPointsChanged(String playerId, int amount){
        if(playerId == player1Id){
            updatePlayer1Score(amount);
        }
        else if(playerId == player2Id){
            updatePlayer2Score(amount);
        }
        updateMatchSession();
    }

    private void onGameEnded(){
        startNextGame();
    }

    // TODO: BAD
    private void onActivePlayerChanged(String playerId){
         activePlayer = playerId;
         updateMatchSession();
    }

    public void setOnMatchUpdatedListener(OnMatchUpdatedListener listener) {
        this.onMatchUpdatedListener = listener;
    }

    public void updateMatchSession(){
        if (onMatchUpdatedListener != null) {
            onMatchUpdatedListener.onMatchUpdated(this); // always update local UI/VM
        }
//        if(!isMyTurn()) return; // uhhh
        MatchSessionData data = new MatchSessionData(
                null,
                player1Id,
                player2Id,
                player1Score,
                player2Score,
                currentGameId,
                activePlayer
        );
        this.matchService.update(id, data);
    }

    public void onRemoteMatchUpdated(MatchSessionData data) {
        boolean gameChanged = data.getCurrentGameId() != this.currentGameId;

        this.player1Score = data.getPlayer1Score();
        this.player2Score = data.getPlayer2Score();
        this.activePlayer = data.getActivePlayer();
        this.currentGameId = data.getCurrentGameId();

        if (onMatchUpdatedListener != null) {
            onMatchUpdatedListener.onMatchUpdated(this);
        }

        if (gameChanged && (currentGame == null || currentGame.getId() != this.currentGameId)) {
            attachLocalGameForCurrentId();
        }

        if (this.currentGameId == 0) {
            // match ended remotely
            Log.d("Match", "ENDING MATCH on remote");

            Log.d("Match", "Match ended (remote)!");
            // here?
        }
    }

    private void handleRemoteMatchEvent(MatchSessionData data) {
        if (data == null) {
            // Document was deleted — match ended remotely
            this.currentGameId = 0;
            if (onMatchUpdatedListener != null) {
                onMatchUpdatedListener.onMatchUpdated(this);
            }
            Log.d("Match", "Match ended (remote, doc deleted)!");

            if(matchType == MatchType.CLASSIC){  // Za klasicnu partiju obracunavaju se zvezde i rang lista.
                resolveClassicRewards();
            }

            return;
        }
        onRemoteMatchUpdated(data);
    }

    private void attachLocalGameForCurrentId() {
        switch (currentGameId) {
            case 5:
                GameSession session = new GameSession(id, player1Id, player2Id);
                KorakPoKorak game = new KorakPoKorak(session, korakPoKorakService, sessionManager);
                game.setOnActivePlayerChangedListener(this::onActivePlayerChanged);
                game.setOnPointsChangedListener(this::onPointsChanged);
                game.setOnGameEndedListener(this::onGameEnded);
                game.getGameService().observeSessionData(id, game::onRemoteSessionUpdated);
                currentGame = game;
                break;
            case 6:
                GameSession mbSession = new GameSession(id, player1Id, player2Id);
                MojBroj mbGame = new MojBroj(mbSession, mojBrojService);
                mbGame.setOnActivePlayerChangedListener(this::onActivePlayerChanged);
                mbGame.setOnPointsChangedListener(this::onPointsChanged);
                mbGame.setOnGameEndedListener(this::onGameEnded);
                currentGame = mbGame;
                break;
        }
    }

    private boolean isMyTurn(){
        boolean myTurn = Objects.equals(activePlayer, sessionManager.getCurrentUserId());
        Log.d("Match", "My turn: " + myTurn);
        return myTurn;
    }
}
