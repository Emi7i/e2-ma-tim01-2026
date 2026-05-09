package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaKolona;
import com.example.slagalica.domain.model.match.games.AsocijacijaPolje;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AsocijacijeDemoFactory {

    public Asocijacija createDemoAsocijacija() {
        List<AsocijacijaKolona> columns = new ArrayList<>();

        columns.add(new AsocijacijaKolona(
                "A",
                Arrays.asList(
                        new AsocijacijaPolje("CVET", false),
                        new AsocijacijaPolje("DRVO", false),
                        new AsocijacijaPolje("MEDVED", false),
                        new AsocijacijaPolje("BUDJENJE", false)
                ),
                "PROLECE",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "B",
                Arrays.asList(
                        new AsocijacijaPolje("SNEG", false),
                        new AsocijacijaPolje("LED", false),
                        new AsocijacijaPolje("HLADNO", false),
                        new AsocijacijaPolje("PLANINA", false)
                ),
                "ZIMA",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "C",
                Arrays.asList(
                        new AsocijacijaPolje("MORE", false),
                        new AsocijacijaPolje("PESAK", false),
                        new AsocijacijaPolje("ODMOR", false),
                        new AsocijacijaPolje("SUNCE", false)
                ),
                "LETO",
                false
        ));

        columns.add(new AsocijacijaKolona(
                "D",
                Arrays.asList(
                        new AsocijacijaPolje("LIST", false),
                        new AsocijacijaPolje("KIŠA", false),
                        new AsocijacijaPolje("VETAR", false),
                        new AsocijacijaPolje("MAGLA", false)
                ),
                "JESEN",
                false
        ));

        return new Asocijacija(
                "Igrač 1",
                "Igrač 2",
                "02:00",
                columns,
                "GODISNJA DOBA",
                false
        );
    }
}