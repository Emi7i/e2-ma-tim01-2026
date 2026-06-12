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
            new KoZnaZna("5", "Koji je hemijski simbol za zlato?", "Au", Arrays.asList("Ag", "Fe", "Cu")),
            new KoZnaZna("6", "Koja reka protiče kroz Beograd?", "Dunav", Arrays.asList("Sava", "Tisa", "Morava")),
            new KoZnaZna("7", "Koji je najviši planinski vrh na svetu?", "Mont Everest", Arrays.asList("K2", "Kilimandžaro", "Mon Blan")),
            new KoZnaZna("8", "Koje godine je počeo Prvi svetski rat?", "1914", Arrays.asList("1912", "1918", "1939")),
            new KoZnaZna("9", "Koja je najmanja država na svetu?", "Vatikan", Arrays.asList("Monako", "San Marino", "Andora")),
            new KoZnaZna("10", "Koliko igrača ima u fudbalskom timu na terenu?", "11", Arrays.asList("10", "12", "9")),
            new KoZnaZna("11", "Koji okean je najveći?", "Tihi okean", Arrays.asList("Atlantski okean", "Indijski okean", "Severni ledeni okean")),
            new KoZnaZna("12", "Ko je naslikao Mona Lizu?", "Leonardo da Vinči", Arrays.asList("Mikelanđelo", "Pablo Pikaso", "Vinsent van Gog")),
            new KoZnaZna("13", "Koji je najbrži kopneni sisar?", "Gepard", Arrays.asList("Lav", "Antilopa", "Konj")),
            new KoZnaZna("14", "Koji gas biljke uzimaju iz vazduha za fotosintezu?", "Ugljen-dioksid", Arrays.asList("Kiseonik", "Azot", "Vodonik")),
            new KoZnaZna("15", "Koji je glavni grad Italije?", "Rim", Arrays.asList("Milano", "Napulj", "Venecija"))
        );
    }
}
