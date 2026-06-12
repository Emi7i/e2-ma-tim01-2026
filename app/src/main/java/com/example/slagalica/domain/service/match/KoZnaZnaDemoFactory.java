package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.KoZnaZna;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KoZnaZnaDemoFactory {

    public List<KoZnaZna> createDemoQuestions() {
        return Arrays.asList(
            new KoZnaZna("1", "Koji je glavni grad Francuske?", "Pariz", Arrays.asList("London", "Berlin", "Rim")),
            new KoZnaZna("2", "Koja je najveća planeta u Sunčevom sistemu?", "Jupiter", Arrays.asList("Mars", "Saturn", "Zemlja")),
            new KoZnaZna("3", "Koliko kontinenata postoji?", "7", Arrays.asList("5", "6", "8")),
            new KoZnaZna("4", "Ko je napisao 'Na Drini ćuprija'?", "Ivo Andrić", Arrays.asList("Meša Selimović", "Miloš Crnjanski", "Borisav Stanković")),
            new KoZnaZna("5", "Koji je hemijski simbol za zlato?", "Au", Arrays.asList("Ag", "Fe", "Cu"))
        );
    }
}
