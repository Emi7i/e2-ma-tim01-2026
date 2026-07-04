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

    private OnMatchUpdatedListener onMatchUpdatedListener;

    public Match(
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
        this.currentGameId = 1;

        MatchSessionData data = new MatchSessionData(
                null,
                player1Id,
                player2Id,
                player1Score,
                player2Score,
                this.currentGameId,
                player1Id
        );

        this.matchService.create(data)
                .thenAccept(matchId -> {
                    this.id = matchId;
                    Log.d("Match", "Match created");
                    if (onReadyCallback != null) onReadyCallback.run();
                })
                .exceptionally(throwable -> {
                    // handle error
                    return null;
                });
    }

    public void startNextGame(){
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
        // startMojBroj(); // for fast testing!
        startKoZnaZna();
    }

    public void endMatch(){
        CompletableFuture<Void> completion;

        if(matchType == MatchType.CLASSIC){  // Za klasicnu partiju obracunavaju se zvezde i rang lista.
            completion = resolveClassicRewards();
        } else {
            completion = CompletableFuture.completedFuture(null);   //za ostale tipove se ne obradjuju nagrade
        }
        currentGameId = 0; // signal game over
        updateMatchSession();

        completion.whenComplete((ignored, throwable) -> {    // Match sesija se brise tek nakon zavrsene obrade nagrada.
            if (throwable != null) {
                Log.e(
                        "Match",
                        "Failed to resolve match rewards",
                        throwable
                );
            }

            matchService.delete(id)
                    .exceptionally(error -> {
                        Log.e(
                                "Match",
                                "Failed to delete match session",
                                error
                        );
                        return null;
                    });
        });

        Log.d("Match", "Match ended!");
    }

    // Resolves rewards for classic match.
    // Add other methods for other scenarios,
    // for example tournament? or do that elsewhere
    private CompletableFuture<Void> resolveClassicRewards(){
        // player 1 is winner even if they have the same points >:D
        String winnerId = player1Score >= player2Score ? player1Id : player2Id;
        CompletableFuture<UserProfile> player1Future = userProfileRepository.getProfile(player1Id);     //ucitavanje profila oba igraca
        CompletableFuture<UserProfile> player2Future = userProfileRepository.getProfile(player2Id);

        return CompletableFuture.allOf(player1Future, player2Future)
                .thenCompose(ignored -> {
                    UserProfile player1 = player1Future.join();
                    UserProfile player2 = player2Future.join();

                    if (player1 == null || player2 == null) {
                        throw new IllegalStateException(
                                "Profil jednog od igraca ne postoji."
                        );
                    }

                    long oldPlayer1Stars = player1.getNumStars();       //cuvamo staro stanje za racunanje promene zvezda
                    long oldPlayer2Stars = player2.getNumStars();

                    // stars per 40 points
                    int additionalStars = player1Score / 40;
                    player1.setNumStars(player1.getNumStars() + additionalStars);
                    additionalStars = player2Score / 40;
                    player2.setNumStars(player2.getNumStars() + additionalStars);

                    // winner/loser stars
                    if(Objects.equals(player1.getUserId(), winnerId)){
                        player1.setNumStars(player1.getNumStars() + 10);
                        long loserStars = player2.getNumStars() - 10;
                        player2.setNumStars(Math.max(0, loserStars));
                    }
                    else{
                        player2.setNumStars(player2.getNumStars() + 10);
                        long loserStars = player1.getNumStars() - 10;
                        player1.setNumStars(Math.max(0, loserStars));
                    }

                    long player1Delta = player1.getNumStars() - oldPlayer1Stars;   //zvedze za ovu partiju
                    long player2Delta = player2.getNumStars() - oldPlayer2Stars;

                    //cuvanje oba profila
                    CompletableFuture<Void> saveProfiles = CompletableFuture.allOf(userProfileRepository.saveProfile(player1), userProfileRepository.saveProfile(player2));


                    return saveProfiles.thenCompose(saveIgnored -> {
                        String myId = sessionManager.getCurrentUserId();
                        if (Objects.equals(player1.getUserId(), myId)) {        //refresh profila
                            sessionManager.setCurrentProfile(player1);
                        } else if (Objects.equals(player2.getUserId(), myId)) {
                            sessionManager.setCurrentProfile(player2);
                        }

                        return rankingRepository.recordClassicMatch(           // upis rezultata u nedeljnu i mesecnu rang listu.
                                id,
                                player1,
                                player1Delta,
                                player2,
                                player2Delta,
                                System.currentTimeMillis()
                        );
                    });
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
        KorakPoKorak game = new KorakPoKorak(session, korakPoKorakService);
        game.setOnActivePlayerChangedListener(this::onActivePlayerChanged);
        game.setOnPointsChangedListener(this::onPointsChanged);
        game.setOnGameEndedListener(this::onGameEnded);
        currentGameId = game.getId();
        currentGame = game;
        updateMatchSession();
    }

    public void startMojBroj() {
        GameSession session = new GameSession(id, player1Id, player2Id);
        MojBroj game = new MojBroj(session, mojBrojService);
        game.setOnActivePlayerChangedListener(this::onActivePlayerChanged);
        game.setOnPointsChangedListener(this::onPointsChanged);
        game.setOnGameEndedListener(this::onGameEnded);
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
        onMatchUpdatedListener.onMatchUpdated(this);
    }
}
