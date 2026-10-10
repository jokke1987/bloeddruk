# Bloeddruk-app (Kotlin, Jetpack Compose, Room)

## Starten
1. Open de map `Bloeddruk` in **Android Studio** (Koala of nieuwer).
2. Laat Gradle synchroniseren (Android Studio maakt de Gradle-wrapper zelf aan).
3. Kies je telefoon of emulator en druk op **Run**.

## Wat zit erin
- **Overzicht** (tab): tabel `DD | MM | JJ`, `UU:MM`, `Bovendruk | Onderdruk`, `BPM` met kleurkoppen en icoontjes.
- **Grafiek** (tab): lijndiagram van bovendruk en onderdruk, periode 7 d / 30 d / 90 d / Alles, gemiddelden (alles, ochtend, avond), tik op een punt voor details.
- **Kleurcodering**: waarden in een subtiele pil: blauw = laag, groen = normaal, oranje = licht verhoogd, rood = hoog. Grenzen staan in `ui/BpLevel.kt` en zijn daar aan te passen.
- **Notities en labels**: bij elke meting kies je labels (Na het sporten, Rustmoment, Medicatie ingenomen, ...) en typ je een notitie. Ze staan onder de rij in het overzicht, in de grafiek-details en in de export. De vaste labels staan in `ui/Tags.kt`.
- **Export** (deel-icoon rechtsboven): kies een periode en exporteer als **PDF-rapport** (samenvatting, grafiek en tabel) of **CSV** (opent in Excel). Daarna kies je: **Openen** (direct bekijken op de telefoon), **Opslaan** (in Downloads/Bloeddruk) of **Delen** (mail, WhatsApp, Drive).
- **Animatie**: kloppend hart en voortgangsbalk "Vandaag x van 2 metingen".
- **Invoer** (BottomSheet): Ochtend/Avond-schakelaar, datum en tijd vooraf op nu (aanpasbaar), waarden, labels en notitie. Tik op een rij om te wijzigen of te verwijderen.
- **Alarmen** (wekkericoon): 4 klokken, 2 voor ochtend en 2 voor avond, dagelijks herhaald, ook na een herstart.
- **Opslag**: Room database `bloeddruk.db`, lokaal op de telefoon. Bij een update van een eerdere versie blijven bestaande metingen behouden (migratie 1 naar 2).

## Bestanden
- `data/` : `BloodPressureEntry`, `AlarmSetting`, DAO's, `AppDatabase` (versie 2)
- `alarm/` : `AlarmScheduler`, `AlarmReceiver`, `BootReceiver`
- `export/` : `Exporter` (PDF en CSV)
- `ui/` : `OverviewScreen`, `ChartScreen`, `EntrySheet`, `ExportDialog`, `AlarmsScreen`, `BpLevel`, `Tags`, `Theme`
- `MainActivity.kt`, `MainViewModel.kt`, `BloeddrukApp.kt`

De kleurcodes en het rapport zijn indicatief en geen medische diagnose.
