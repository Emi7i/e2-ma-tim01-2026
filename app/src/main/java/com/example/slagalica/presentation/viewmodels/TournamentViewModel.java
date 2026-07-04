package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.tournament.TournamentMatch;
import com.example.slagalica.domain.model.tournament.TournamentRound;
import com.example.slagalica.domain.model.tournament.TournamentSession;
import com.example.slagalica.repository.impl.TournamentRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TournamentViewModel extends ViewModel {

    private final TournamentRepository tournamentRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<TournamentSession> tournament = new MutableLiveData<>();
    private final MutableLiveData<TournamentMatch> nextMatch = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> waitingInQueue = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> info = new MutableLiveData<>();

    @Inject
    public TournamentViewModel(
            TournamentRepository tournamentRepository,
            SessionManager sessionManager
    ) {
        this.tournamentRepository = tournamentRepository;
        this.sessionManager = sessionManager;
    }

    public LiveData<TournamentSession> getTournament() {
        return tournament;
    }

    public LiveData<TournamentMatch> getNextMatch() {
        return nextMatch;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getWaitingInQueue() {
        return waitingInQueue;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getInfo() {
        return info;
    }

    public void loadActiveTournament() {
        String userId = sessionManager.getCurrentUserId();
        if (userId == null) {
            error.setValue("Korisnik nije prijavljen.");
            return;
        }

        loading.setValue(true);
        tournamentRepository.getActiveTournamentForUser(userId)
                .thenAccept(activeTournament -> {
                    tournament.postValue(activeTournament);
                    if (activeTournament != null) {
                        loadNextMatch(activeTournament.getTournamentId());
                    }
                    loading.postValue(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    public void joinTournament() {
        UserProfile currentProfile = sessionManager.getCurrentProfile().getValue();
        if (currentProfile == null) {
            error.setValue("Profil korisnika nije učitan.");
            return;
        }

        loading.setValue(true);
        tournamentRepository.joinTournamentQueue(currentProfile)
                .thenAccept(result -> {
                    tournament.postValue(result);
                    if (result == null) {
                        waitingInQueue.postValue(true);
                        nextMatch.postValue(null);
                        info.postValue("Ušli ste u red za turnir. Čekaju se još igrači.");
                    } else {
                        waitingInQueue.postValue(false);
                        info.postValue("Turnir je formiran.");
                        loadNextMatch(result.getTournamentId());
                    }
                    sessionManager.loadCurrentProfile();
                    loading.postValue(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    public void createDemoTournament() {
        UserProfile currentProfile = sessionManager.getCurrentProfile().getValue();
        if (currentProfile == null) {
            error.setValue("Profil korisnika nije učitan.");
            return;
        }

        loading.setValue(true);
        tournamentRepository.createDemoTournament(currentProfile)
                .thenAccept(result -> {
                    waitingInQueue.postValue(false);
                    tournament.postValue(result);
                    info.postValue("Demo turnir je kreiran za testiranje na jednom uređaju.");
                    if (result != null) {
                        loadNextMatch(result.getTournamentId());
                    }
                    sessionManager.loadCurrentProfile();
                    loading.postValue(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    public void cancelQueue() {
        String userId = sessionManager.getCurrentUserId();
        if (userId == null) {
            return;
        }

        loading.setValue(true);
        tournamentRepository.cancelWaitingQueue(userId)
                .thenAccept(ignored -> {
                    waitingInQueue.postValue(false);
                    tournament.postValue(null);
                    nextMatch.postValue(null);
                    info.postValue("Izašli ste iz reda za turnir.");
                    loading.postValue(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    public void loadNextMatch(String tournamentId) {
        String userId = sessionManager.getCurrentUserId();
        if (tournamentId == null || userId == null) {
            return;
        }

        tournamentRepository.getNextPlayableMatchForUser(tournamentId, userId)
                .thenAccept(nextMatch::postValue)
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    return null;
                });
    }

    public void simulateWinCurrentMatch() {
        TournamentSession currentTournament = tournament.getValue();
        TournamentMatch match = nextMatch.getValue();
        String currentUserId = sessionManager.getCurrentUserId();

        if (currentTournament == null || match == null || currentUserId == null) {
            error.setValue("Nema aktivne turnirske partije.");
            return;
        }

        int player1Score = currentUserId.equals(match.getPlayer1Id()) ? 120 : 40;
        int player2Score = currentUserId.equals(match.getPlayer2Id()) ? 120 : 40;

        loading.setValue(true);
        tournamentRepository.recordTournamentMatchResult(
                        currentTournament.getTournamentId(),
                        match.getMatchId(),
                        match.getRoundEnum(),
                        match.getPlayer1Id(),
                        match.getPlayer2Id(),
                        player1Score,
                        player2Score
                )
                .thenAccept(ignored -> {
                    info.postValue("Rezultat turnirske partije je upisan.");
                    sessionManager.loadCurrentProfile();
                    loadActiveTournament();
                    loading.postValue(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    public void recordRealMatchResult(
            TournamentMatch match,
            int player1Score,
            int player2Score
    ) {
        TournamentSession currentTournament = tournament.getValue();
        if (currentTournament == null || match == null) {
            error.setValue("Nema aktivne turnirske partije.");
            return;
        }

        loading.setValue(true);
        tournamentRepository.recordTournamentMatchResult(
                        currentTournament.getTournamentId(),
                        match.getMatchId(),
                        match.getRoundEnum(),
                        match.getPlayer1Id(),
                        match.getPlayer2Id(),
                        player1Score,
                        player2Score
                )
                .thenAccept(ignored -> {
                    sessionManager.loadCurrentProfile();
                    loadActiveTournament();
                    loading.postValue(false);
                })
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }
}
