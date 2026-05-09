# Slagalica
Projekat iz mobilnih aplikacija

- RA81/2022 Katarina Vujović
- RA108/2022 Emilija Opsenica
- RA169/2022 Kristina Petrović

## 🎢 Pokretanje aplikacije

### Requirements
- Android Studio 
- JDK 8 or higher
- Android SDK with API level 21 or higher
- Emulator ili Android uređaj za testiranje

### Koraci za pokretanje

#### 1. Kloniranje repozitorijuma
   ```bash
   git clone https://github.com/Emi7i/e2-ma-tim01-2026.git
   cd e2-ma-tim01-2026
   ```

#### 2. Otvaranje projekta u Android Studio
   - Otvorite Android Studio
   - Select "Open an Existing Project"
   - Navigirajte do foldera gde se nalazi projekat i selektujte ga

#### 3. Sinhronizacija Gradle-a
   - Android Studio će automatski tražiti da sinhronizujete Gradle
   - Kliknite "Sync Now" ako se pojavi notifikacija
   - Sačekajte da se završi sinhronizacija 

#### 4. Pokretanje aplikacije
   - Povežite Android uređaj preko USB-a ili pokrenite emulator
   - Selektujte željeni uređaj iz liste dostupnih uređaja
   - Kliknite na "Run" dugme u toolbar-u
   - Ili pritisnite `Shift + F10`

### Alternativno: Pokretanje preko komandne linije

```bash
# Navigirajte u root foldera
cd e2-ma-tim01-2026

# Na Windows:
./gradlew assembleDebug

# Za pokretanje na emulatoru/uređaju:
./gradlew installDebug
```

## 🐇 Activities
MainActivity samo preusmerava na koju sledeću aktivnost da se ode kad se uđe u aplikaciju.
AuthActivity prikazuje login i register fragmente
AppActivity je glavni za sve drugo - igre, chat, notifs...

### Burger meni
Dizajn (linkovi) je u res/view_drawer_left.xml
Gde vode linkovi se menja u [AppActivity](https://github.com/Emi7i/e2-ma-tim01-2026/blob/d526a52a8ed7143bc09f719db890c36f6b99acf1/app/src/main/java/com/example/slagalica/presentation/activities/AppActivity.java) sa:

```
// Drawer links
        View leftDrawer = binding.leftDrawer.getHeaderView(0);
        leftDrawer.findViewById(R.id.home).setOnClickListener(v -> {
            FragmentTransition.to(new HomeFragment(), this, false, R.id.appContainer); // ovde fragment na koji treba da vodi. Boolean vrednost odlucuje da li korisnik moze da se vrati na prethodnu stranicu sa back
            binding.main.closeDrawer(GravityCompat.START);
        });
```
### Linkovi do igara
U [res/fragment_home.xml](https://github.com/Emi7i/e2-ma-tim01-2026/blob/d526a52a8ed7143bc09f719db890c36f6b99acf1/app/src/main/res/layout/fragment_home.xml) su privremeno dugmad do svih igara.

Linkovanje do određenog fragmenta se nameštaju u [HomeFragment](https://github.com/Emi7i/e2-ma-tim01-2026/blob/d526a52a8ed7143bc09f719db890c36f6b99acf1/app/src/main/java/com/example/slagalica/presentation/fragments/common/HomeFragment.java):
```
@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Temporary access to all games from home
        binding.korakPoKorak.setOnClickListener(v -> {
            FragmentTransition.to(new KorakPoKorakFragment(), requireActivity(), true, R.id.appContainer); // prosledi se klasa fragmenta opet
        });
    }
```

### Game fragment
Kad se učita game fragment se automatski pojavi timer [GameHeader](https://github.com/Emi7i/e2-ma-tim01-2026/blob/d526a52a8ed7143bc09f719db890c36f6b99acf1/app/src/main/java/com/example/slagalica/presentation/views/GameHeaderView.java) gore. Da bi se to desilo mora u fragmentu da se instancira match view model i aktivira timer, primer u KorakPoKorakFragment.java:
```
[..]
MatchViewModel matchViewModel;
[...]

@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class); // generic view model creation
        matchViewModel.setGameActive(true); // activates timer header
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matchViewModel.setGameActive(false); // hide timer
        binding = null; // bitno uraditi u svakom fragmentu
    }
```

### Hilt
Da bi vam radio MatchViewModel, pošto koristimo Hilt, bitno je na fragmentima i aktivnostima staviti annotation:
```
@AndroidEntryPoint
public class KorakPoKorakFragment extends Fragment {
[...]
```
