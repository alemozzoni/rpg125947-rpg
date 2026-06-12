# Funzionalità implementate

## Flusso di gioco

1. **Creazione del personaggio** — schermata iniziale con il nome dell'investigatore e la **distribuzione dei punti caratteristica** sulle quattro abilità (Osservazione, Intuito, Eloquenza, Logica); il pulsante _Inizia a Giocare_ è abilitato solo a nome valido (validazione tramite _binding_). Vedi **[Sistema di Ruolo](Sistema-di-Ruolo)**.
2. **Esplorazione delle stanze** — il museo ha sei sale navigabili; ogni sala mostra uno sfondo, i personaggi presenti come sprite e gli oggetti ispezionabili. La navigazione avviene tramite i controlli direzionali delle uscite.
3. **Dialogo e testimonianze** — cliccando un personaggio si apre un pannello di dialogo con scelte multiple di domanda; le risposte (testimonianze) vengono annotate automaticamente nel taccuino, alcune rivelano indizi. Alcune domande sono **prove di Eloquenza** (🎲): solo superandole il testimone parla. Le domande proposte dipendono inoltre dallo **stile investigativo** del personaggio: profili diversi pongono domande diverse (contrassegnate da **✦**) e ottengono risposte caratterizzate, pur potendo tutti raggiungere la **stessa soluzione**. Vedi **[Sistema di Ruolo](Sistema-di-Ruolo)**.
4. **Investigazione ambientale** — gli _hotspot_ si illuminano al passaggio del mouse; cliccandoli si ispeziona l'oggetto e si possono scoprire indizi, che entrano nel taccuino senza duplicati. Gli indizi più nascosti richiedono una **prova di abilità** (Osservazione, Intuito o Logica): un tiro **d20 + attributo** contro una difficoltà.
5. **Progressione** — ogni indizio raccolto dà **esperienza**; salendo di livello si ottengono **punti abilità** da investire sugli attributi dalla scheda _Investigatore_ del taccuino. L'HUD di gioco mostra in basso a sinistra, sempre aggiornati, **livello** e **punti abilità disponibili**.
6. **Taccuino** — finestra a schede (Investigatore con scheda e progressione, Indizi, Testimonianze raggruppate per fonte e **senza duplicati**, Sospettati con movente e alibi, Appunti liberi editabili). È una vista in sola lettura sul modello (salvo appunti e spesa dei punti abilità).
7. **Menu di gioco** — un unico pulsante **Menu** apre un pannello con **Salva**, **Carica** ed **Esci al menu principale** (con conferma). La partita è salvata su file XML e ricaricabile in qualunque momento, **scheda personaggio e progressione incluse** (persistenza su memoria non volatile).
8. **Accusa e finale** — il giocatore sceglie il sospettato e gli indizi a supporto; il sistema valuta l'accusa e mostra l'epilogo. In caso di fallimento è possibile ricaricare un salvataggio e riprovare.

## La meccanica investigativa

Il caso si risolve **ragionando con i concetti del corso**, intrecciati alla trama:

- l'**anacronismo** di un manoscritto attribuito a Von Neumann (1945) che cita il Teorema di Böhm-Jacopini (1966), smascherato confrontandolo con la cronologia dei linguaggi;
- il **log degli accessi** (un _event log_ alla base degli Event Knowledge Graph) che colloca un badge sulla scena del crimine;
- l'**integrità crittografica** del registro (hash a catena, come i commit di Git) che ne garantisce l'attendibilità e scagiona un sospettato;
- la **perizia** del direttore, che fornisce il movente.

L'accusa ha successo **solo** indicando il colpevole corretto **e** presentando tutti gli indizi decisivi. La regola di vittoria è incapsulata in una _Strategy_ (`ValutatoreAccusa`) ed è quindi facilmente sostituibile.

Scoprire questi indizi non è scontato: gli snodi chiave sono **prove di abilità** (vedi [Sistema di Ruolo](Sistema-di-Ruolo)) che mettono alla prova le caratteristiche dell'investigatore, anch'esse risolte da una _Strategy_ intercambiabile (`RisolutoreProva`).

## Requisiti di consegna soddisfatti

- ✅ Java 25, package `it.unicam.cs.mpgc.rpg125947`.
- ✅ `./gradlew build` e `./gradlew run` sufficienti (Gradle Wrapper versionato).
- ✅ Interfaccia **grafica** (JavaFX), non testuale.
- ✅ **Persistenza** dei dati (salvataggio/caricamento partita) operativa.
- ✅ README con descrizione, istruzioni e dichiarazione AI; Wiki di documentazione.
- ✅ Architettura predisposta a future estensioni multi-dispositivo.
