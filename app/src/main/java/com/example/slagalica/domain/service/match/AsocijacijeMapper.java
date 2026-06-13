package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;
import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaColumnDocument;
import com.example.slagalica.domain.model.match.games.AsocijacijaDocument;
import com.example.slagalica.domain.model.match.games.AsocijacijaKolona;
import com.example.slagalica.domain.model.match.games.AsocijacijaPolje;

import java.util.ArrayList;
import java.util.List;

public class AsocijacijeMapper {

    public Asocijacija toRuntime(AsocijacijaDocument source, int roundNumber, int startingPlayer) {
        List<AsocijacijaKolona> runtimeColumns = new ArrayList<>();

        for (AsocijacijaColumnDocument columnDocument : source.getColumns()) {
            List<AsocijacijaPolje> runtimeFields = new ArrayList<>();

            for (String fieldText : columnDocument.getFields()) {
                runtimeFields.add(new AsocijacijaPolje(fieldText, false));
            }

            runtimeColumns.add(new AsocijacijaKolona(
                    columnDocument.getLabel(),
                    runtimeFields,
                    columnDocument.getSolution(),
                    false
            ));
        }

        TwoPlayerGameState gameState = new TwoPlayerGameState(
                "Igrač 1",
                "Igrač 2",
                roundNumber,
                startingPlayer,
                startingPlayer,
                120,
                0,
                0,
                false,
                false
        );

        return new Asocijacija(
                gameState,
                runtimeColumns,
                source.getFinalSolution()
        );
    }
}