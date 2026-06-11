package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.TwoPlayerGameState;
import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaKolona;
import com.example.slagalica.domain.model.match.games.AsocijacijaPolje;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AsocijacijeDemoFactory {

    public List<Asocijacija> createDemoRounds() {
        List<Asocijacija> allRounds = new ArrayList<>();
        allRounds.add(createRoundOneTemplate());
        allRounds.add(createRoundTwoTemplate());
        allRounds.add(createRoundThreeTemplate());
        allRounds.add(createRoundFourTemplate());

        Collections.shuffle(allRounds);

        List<Asocijacija> selectedRounds = new ArrayList<>();
        selectedRounds.add(copyForMatch(allRounds.get(0), 1, 1));
        selectedRounds.add(copyForMatch(allRounds.get(1), 2, 2));

        return selectedRounds;
    }

    private Asocijacija copyForMatch(Asocijacija source, int roundNumber, int startingPlayer) {
        List<AsocijacijaKolona> copiedColumns = new ArrayList<>();

        for (AsocijacijaKolona column : source.getColumns()) {
            List<AsocijacijaPolje> copiedFields = new ArrayList<>();

            for (AsocijacijaPolje field : column.getFields()) {
                copiedFields.add(new AsocijacijaPolje(field.getText(), false));
            }

            copiedColumns.add(new AsocijacijaKolona(
                    column.getLabel(),
                    copiedFields,
                    column.getSolution(),
                    false
            ));
        }

        TwoPlayerGameState gameState = new TwoPlayerGameState(
                source.getGameState().getPlayerOneName(),
                source.getGameState().getPlayerTwoName(),
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
                copiedColumns,
                source.getFinalSolution()
        );
    }

    private Asocijacija createRoundOneTemplate() {
        List<AsocijacijaKolona> columns = new ArrayList<>();

        columns.add(new AsocijacijaKolona(
                "A",
                Arrays.asList(
                        new AsocijacijaPolje("MORE", false),
                        new AsocijacijaPolje("POSADA", false),
                        new AsocijacijaPolje("SVEMIR", false),
                        new AsocijacijaPolje("MORNAR", false)
                ),
                "BROD",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "B",
                Arrays.asList(
                        new AsocijacijaPolje("VAZDUH", false),
                        new AsocijacijaPolje("O2", false),
                        new AsocijacijaPolje("GAS", false),
                        new AsocijacijaPolje("PLUĆA", false)
                ),
                "KISEONIK",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "C",
                Arrays.asList(
                        new AsocijacijaPolje("POSLATI", false),
                        new AsocijacijaPolje("RAZMENA", false),
                        new AsocijacijaPolje("SMS", false),
                        new AsocijacijaPolje("PISMO", false)
                ),
                "PORUKA",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "D",
                Arrays.asList(
                        new AsocijacijaPolje("PROZOR", false),
                        new AsocijacijaPolje("LUSTER", false),
                        new AsocijacijaPolje("RAZBITI", false),
                        new AsocijacijaPolje("ČAŠA", false)
                ),
                "STAKLO",
                false
        ));

        return new Asocijacija(
                new TwoPlayerGameState(
                        "Igrač 1",
                        "Igrač 2",
                        0,
                        0,
                        0,
                        120,
                        0,
                        0,
                        false,
                        false
                ),
                columns,
                "BOCA"
        );
    }

    private Asocijacija createRoundTwoTemplate() {
        List<AsocijacijaKolona> columns = new ArrayList<>();

        columns.add(new AsocijacijaKolona(
                "A",
                Arrays.asList(
                        new AsocijacijaPolje("PLAVA", false),
                        new AsocijacijaPolje("RAJSKA", false),
                        new AsocijacijaPolje("TRKAČICA", false),
                        new AsocijacijaPolje("JAJA", false)
                ),
                "PTICA",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "B",
                Arrays.asList(
                        new AsocijacijaPolje("SUŠENO", false),
                        new AsocijacijaPolje("BOBIČASTO", false),
                        new AsocijacijaPolje("JUŽNO", false),
                        new AsocijacijaPolje("KRUŠKA", false)
                ),
                "VOĆE",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "C",
                Arrays.asList(
                        new AsocijacijaPolje("KOSA", false),
                        new AsocijacijaPolje("OČI", false),
                        new AsocijacijaPolje("KAFENA", false),
                        new AsocijacijaPolje("MEDVED", false)
                ),
                "SMEĐA",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "D",
                Arrays.asList(
                        new AsocijacijaPolje("GOSPODAR PRSTENOVA", false),
                        new AsocijacijaPolje("OSTRVA", false),
                        new AsocijacijaPolje("MAORI", false),
                        new AsocijacijaPolje("AUSTRALIJA", false)
                ),
                "NOVI ZELAND",
                false
        ));

        return new Asocijacija(
                new TwoPlayerGameState(
                        "Igrač 1",
                        "Igrač 2",
                        0,
                        0,
                        0,
                        120,
                        0,
                        0,
                        false,
                        false
                ),
                columns,
                "KIVI"
        );
    }

    private Asocijacija createRoundThreeTemplate() {
        List<AsocijacijaKolona> columns = new ArrayList<>();

        columns.add(new AsocijacijaKolona(
                "A",
                Arrays.asList(
                        new AsocijacijaPolje("VELIKI", false),
                        new AsocijacijaPolje("MALI", false),
                        new AsocijacijaPolje("POLARNI", false),
                        new AsocijacijaPolje("MED", false)
                ),
                "MEDVED",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "B",
                Arrays.asList(
                        new AsocijacijaPolje("BATAK", false),
                        new AsocijacijaPolje("BARA", false),
                        new AsocijacijaPolje("RODA", false),
                        new AsocijacijaPolje("LOKVANJ", false)
                ),
                "ŽABA",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "C",
                Arrays.asList(
                        new AsocijacijaPolje("GRIVA", false),
                        new AsocijacijaPolje("ŠARAC", false),
                        new AsocijacijaPolje("SNAGA", false),
                        new AsocijacijaPolje("SEDLO", false)
                ),
                "KONJ",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "D",
                Arrays.asList(
                        new AsocijacijaPolje("MAĆEHA", false),
                        new AsocijacijaPolje("BAL", false),
                        new AsocijacijaPolje("VILA", false),
                        new AsocijacijaPolje("ŠTIKLA", false)
                ),
                "PEPELJUGA",
                false
        ));

        return new Asocijacija(
                new TwoPlayerGameState(
                        "Igrač 1",
                        "Igrač 2",
                        0,
                        0,
                        0,
                        120,
                        0,
                        0,
                        false,
                        false
                ),
                columns,
                "BAJKE"
        );
    }

    private Asocijacija createRoundFourTemplate() {
        List<AsocijacijaKolona> columns = new ArrayList<>();

        columns.add(new AsocijacijaKolona(
                "A",
                Arrays.asList(
                        new AsocijacijaPolje("JUG", false),
                        new AsocijacijaPolje("OLUJA", false),
                        new AsocijacijaPolje("POVETARAC", false),
                        new AsocijacijaPolje("ZMAJ", false)
                ),
                "VETAR",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "B",
                Arrays.asList(
                        new AsocijacijaPolje("KRV", false),
                        new AsocijacijaPolje("BOJA", false),
                        new AsocijacijaPolje("CIGLA", false),
                        new AsocijacijaPolje("PAPRIKA", false)
                ),
                "CRVENA",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "C",
                Arrays.asList(
                        new AsocijacijaPolje("BODLJA", false),
                        new AsocijacijaPolje("ZVEZDA", false),
                        new AsocijacijaPolje("OŠTAR", false),
                        new AsocijacijaPolje("OKO", false)
                ),
                "VID",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "D",
                Arrays.asList(
                        new AsocijacijaPolje("PARFEM", false),
                        new AsocijacijaPolje("OPOJAN", false),
                        new AsocijacijaPolje("NOS", false),
                        new AsocijacijaPolje("SVEĆA", false)
                ),
                "MIRIS",
                false
        ));

        return new Asocijacija(
                new TwoPlayerGameState(
                        "Igrač 1",
                        "Igrač 2",
                        0,
                        0,
                        0,
                        120,
                        0,
                        0,
                        false,
                        false
                ),
                columns,
                "RUŽA"
        );
    }
}