package com.example.slagalica.domain.model.match.games.common;

import com.example.slagalica.domain.model.match.MatchmakingEntry;

public interface OnMatchmakingUpdateListener {
    void onMatchFound(MatchmakingEntry entry);
}
