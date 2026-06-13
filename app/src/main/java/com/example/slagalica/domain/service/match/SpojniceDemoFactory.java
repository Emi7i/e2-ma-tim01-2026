package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.Spojnice;
import java.util.Arrays;
import java.util.List;

public class SpojniceDemoFactory {

    public List<Spojnice> createDemoSpojnice() {
        return Arrays.asList(

                new Spojnice("1", "Povežite planine sa njihovim kontinentima",
                        Arrays.asList("Evereest", "Kilimandžaro", "Akongagva", "Monblan", "Denali"),
                        Arrays.asList("Azija", "Afrika", "Južna Amerika", "Evropa", "Severna Amerika")),

                new Spojnice("2", "Povežite hemijske elemente sa njihovim simbolima",
                        Arrays.asList("Zlato", "Gvožđe", "Natrijum", "Kalijum", "Olovo"),
                        Arrays.asList("Au", "Fe", "Na", "K", "Pb")),

                new Spojnice("3", "Povežite kompozitore sa njihovim delima",
                        Arrays.asList("Betoven", "Mocart", "Bah", "Čajkovski", "Šopen"),
                        Arrays.asList("Deveta simfonija", "Čarobna frula", "Tokata i fuga", "Labudovo jezero", "Nokturni")),

                new Spojnice("4", "Povežite filmove sa rediteljem",
                        Arrays.asList("Titanik", "Schindlerova lista", "Matriks", "Prestolonaslednik", "Osmi putnik"),
                        Arrays.asList("Džejms Kameron", "Stiven Spilberg", "Braća Vačovski", "Emir Kusturica", "Ridli Skot")),

                new Spojnice("5", "Povežite sportiste sa njihovim sportom",
                        Arrays.asList("Novak Đoković", "Lionel Mesi", "Majkl Felps", "Jusein Bolt", "Serena Vilijams"),
                        Arrays.asList("Tenis", "Fudbal", "Plivanje", "Atletika", "Tenis")),

                new Spojnice("6", "Povežite grčka božanstva sa njihovim domenima",
                        Arrays.asList("Zevs", "Posejdon", "Artemida", "Hefest", "Dioniz"),
                        Arrays.asList("Grom i nebo", "More", "Lov i mesec", "Vatra i kovačtvo", "Vino i veselje")),

                new Spojnice("7", "Povežite slikare sa njihovim remek-delima",
                        Arrays.asList("Leonardo da Vinči", "Vinsent van Gog", "Pablo Pikaso", "Salvador Dali", "Mikelanđelo"),
                        Arrays.asList("Mona Liza", "Zvezdana noć", "Gernika", "Postojanost pamćenja", "Stvaranje Adama"))

        );
    }
}
