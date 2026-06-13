package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.Spojnice;
import java.util.Arrays;
import java.util.List;

public class SpojniceDemoFactory {

    public List<Spojnice> createDemoSpojnice() {
        return Arrays.asList(
            new Spojnice("1", "Povežite države sa njihovim glavnim gradovima", 
                Arrays.asList("Srbija", "Francuska", "Nemačka", "Italija", "Španija"),
                Arrays.asList("Beograd", "Pariz", "Berlin", "Rim", "Madrid")),
            new Spojnice("2", "Povežite pisce sa njihovim delima", 
                Arrays.asList("Ivo Andrić", "Meša Selimović", "Miloš Crnjanski", "Borisav Stanković", "Danilo Kiš"),
                Arrays.asList("Na Drini ćuprija", "Derviš i smrt", "Seobe", "Nečista krv", "Enciklopedija mrtvih")),
            new Spojnice("3", "Povežite pronalazače sa njihovim izumima", 
                Arrays.asList("Nikola Tesla", "Tomas Edison", "Aleksandar Bel", "Braća Rajt", "Džejms Vat"),
                Arrays.asList("Naizmenična struja", "Sijalica", "Telefon", "Avion", "Parna mašina"))
        );
    }
}
