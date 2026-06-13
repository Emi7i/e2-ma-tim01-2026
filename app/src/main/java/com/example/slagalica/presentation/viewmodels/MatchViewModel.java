package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.match.games.common.GameSession;
import com.example.slagalica.domain.model.match.games.common.IGame;
import com.example.slagalica.domain.model.match.games.korakpokorak.KorakPoKorak;
import com.example.slagalica.domain.service.match.KorakPoKorakService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchViewModel extends ViewModel {
    private final MutableLiveData<IGame> currentGame = new MutableLiveData<>();
    private final KorakPoKorakService korakPoKorakService;

    // MOCKED FOR NOW
    long matchId = 1;
    long player1Id = 1;
    long player2Id = 2;

    @Inject
    public MatchViewModel(
            KorakPoKorakService korakPoKorakService
    ) {
        this.korakPoKorakService = korakPoKorakService;
    }

    // TODO: Add getters and setters for stars (points) and timer
    private final MutableLiveData<Boolean> isGameActive = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsGameActive() { return isGameActive; }

    public void setGameActive(boolean active) { isGameActive.setValue(active); }

    public LiveData<IGame> getCurrentGame() { return currentGame; }

    public void startKorakPoKorak() {
        GameSession session = new GameSession(matchId, player1Id, player2Id);
        KorakPoKorak game = new KorakPoKorak(session, korakPoKorakService);
        currentGame.setValue(game);
    }

}
