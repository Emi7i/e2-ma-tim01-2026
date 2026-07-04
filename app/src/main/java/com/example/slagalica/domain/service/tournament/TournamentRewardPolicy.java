package com.example.slagalica.domain.service.tournament;

import com.example.slagalica.domain.model.tournament.TournamentRewardResult;
import com.example.slagalica.domain.model.tournament.TournamentRound;

public class TournamentRewardPolicy {

    private TournamentRewardPolicy() { }

    public static TournamentRewardResult calculate(
            TournamentRound round,
            int winnerScore,
            long winnerOldStars,
            int loserScore,
            long loserOldStars
    ) {
        long winnerStarsAfterRegularWin = winnerOldStars + 10L + winnerScore / 40L;

        long loserStarsAfterRegularLoss = Math.max(0L, loserOldStars - 10L + loserScore / 40L);

        long winnerDelta = winnerStarsAfterRegularWin - winnerOldStars;
        long loserDelta = loserStarsAfterRegularLoss - loserOldStars;

        if (round == TournamentRound.SEMIFINAL) {
            // Prva pobeda: +2 tokena i regularne zvezde za pobedu.
            // Gubitnik ne dobija nista.
            return new TournamentRewardResult(
                    winnerDelta,
                    0L,
                    2,
                    0
            );
        }

        // Finale: pobednik dobija regularne zvezde + dodatnih 10 zvezda i +3 tokena.
        // Finalni gubitnik dobija regularne zvezde po pravilima poraza.
        return new TournamentRewardResult(
                winnerDelta + 10L,
                loserDelta,
                3,
                0
        );
    }
}
