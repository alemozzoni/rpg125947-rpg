# Architettura e Responsabilità

## Organizzazione in moduli

La build Gradle è **multi-progetto** per separare il dominio dalla presentazione, in vista delle future estensioni multi-dispositivo:

- **`core`** — modello di dominio, logica di gioco e persistenza astratta. È **platform-independent**: non importa nulla di JavaFX. Espone le proprie funzionalità tramite interfacce.
- **`javafx-app`** — front-end desktop JavaFX. Dipende da `core`. Contiene avvio, navigazione tra schermate e controller.

Questa separazione è l'applicazione diretta del **Dependency Inversion Principle**: la UI dipende da astrazioni del core, non viceversa.

## Package del modulo `core`

```
it.unicam.cs.mpgc.rpg125947
├── model              entità di dominio
│   ├── personaggio    Personaggio (astratta), Sospettato, Testimone
│   ├── dialogo        Dialogo, OpzioneDialogo, Testimonianza
│   ├── accusa         Accusa, EsitoAccusa
│   ├── prova          EsitoProva (esito di una prova di abilità)
│   ├── Attributo      caratteristiche dell'investigatore (enum)
│   └── interfaces     CONTRATTI: Interrogabile, Ispezionabile, FaseDiGioco, ValutatoreAccusa, RisolutoreProva
├── logic              MotorePartita, ValutatoreAccusaStandard, RisolutoreProvaDado, RisultatoInterazione
│   └── fase           FaseEsplorazione, FaseDialogo, FaseIspezione, FaseAccusa, FaseFinale (State)
├── persistence        GameStateRepository, ScenarioLoader, PersistenzaException (astrazioni)
│   └── xml            CaricatoreCasoXml, XmlGameStateRepository (implementazioni JAXP)
└── util               Validazioni
```

Il modulo `javafx-app` aggiunge `app` (Launcher, Applicazione, SceneManager, AppContext, RisorseGrafiche, ui), `controller` (i controller FXML) e le risorse `view` (FXML, CSS, immagini).

## Responsabilità di classi e interfacce

### Interfacce (contratti)

| Interfaccia           | Responsabilità                                                                                    |
| --------------------- | ------------------------------------------------------------------------------------------------- |
| `Interrogabile`       | Comportamento di chi può essere interrogato: fornisce un `Dialogo` e le sue testimonianze.        |
| `Ispezionabile`       | Comportamento di un oggetto della scena che può rivelare un indizio quando ispezionato.           |
| `FaseDiGioco`         | Stato del gioco (pattern State): reagisce a un'azione del giocatore decidendo la fase successiva. |
| `ValutatoreAccusa`    | Strategy: stabilisce se un'accusa è corretta.                                                     |
| `RisolutoreProva`     | Strategy: risolve una prova di abilità (skill check) confrontando attributo e difficoltà.         |
| `ScenarioLoader`      | Factory di scenario: costruisce un `Caso` da una fonte esterna nascondendone il formato.          |
| `GameStateRepository` | Repository/DAO: salva e carica lo stato della partita su memoria non volatile.                    |

### Modello di dominio

| Classe                                       | Responsabilità                                                                                                                                                                                      |
| -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Indizio`                                    | Value object immutabile; identità di dominio sull'`id` (`equals`/`hashCode`).                                                                                                                       |
| `Attributo`                                  | Enum delle quattro caratteristiche (Osservazione, Intuito, Eloquenza, Logica) — vedi [Sistema di Ruolo](Sistema-di-Ruolo).                                                                          |
| `Investigatore`                              | **Scheda personaggio**: nome, attributi, livello, esperienza e punti abilità; gestisce avanzamento e potenziamento e calcola lo **stile dominante** (`attributoDominante()`).                       |
| `EsitoProva`                                 | Value object: risultato di un singolo skill check (tiro, attributo, difficoltà, esito).                                                                                                             |
| `Personaggio` (astratta)                     | NPC interrogabile; metodo astratto `isSospettato()`. Identità sul nome.                                                                                                                             |
| `Sospettato` / `Testimone`                   | Sottotipi: il primo aggiunge movente e alibi ed è accusabile; il secondo no.                                                                                                                        |
| `Stanza`                                     | Sala del museo: sfondo, uscite, hotspot, personaggi; può essere chiusa a chiave.                                                                                                                    |
| `Uscita`, `Hotspot`, `Coordinata`            | Collegamento tra stanze, area ispezionabile (con eventuale **prova di abilità**), posizione relativa.                                                                                               |
| `Dialogo`, `OpzioneDialogo`, `Testimonianza` | Conversazione e dichiarazioni; un'opzione può essere una **prova di abilità** e/o essere riservata a uno **stile** investigativo (`Dialogo.opzioniPer`), una testimonianza può rivelare un indizio. |
| `Taccuino`                                   | Raccoglie indizi (`Set`, senza duplicati), testimonianze (`Map` per fonte, **deduplicate**), sospettati e appunti.                                                                                  |
| `Accusa`, `EsitoAccusa`                      | Ipotesi finale (accusato + prove) e relativo esito con narrazione.                                                                                                                                  |
| `Caso`                                       | Aggregato dello scenario: stanze, vittima, catalogo indizi e **soluzione** (colpevole + indizi decisivi).                                                                                           |
| `Partita`                                    | Aggregato radice dello **stato di gioco**: investigatore, caso, stanza corrente, fase, taccuino, esito.                                                                                             |

### Logica

| Classe                     | Responsabilità                                                                                                                                                                                            |
| -------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `MotorePartita`            | Orchestratore delle operazioni applicative (crea/riprendi, muovi, interroga, ispeziona, accusa); risolve le **prove di abilità** e accredita l'esperienza; inietta le Strategy di valutazione e di prova. |
| `ValutatoreAccusaStandard` | Regola di vittoria: colpevole corretto **e** tutti gli indizi decisivi presentati (verifica funzionale con Stream).                                                                                       |
| `RisolutoreProvaDado`      | Strategy di prova a tiro **d20 + attributo ≥ difficoltà**, con sorgente `Random` iniettabile (deterministica nei test).                                                                                   |
| `RisultatoInterazione`     | Record con l'esito ricco di ispezione/interrogatorio: prova tentata, indizio scoperto, esperienza, passaggio di livello.                                                                                  |
| `Fase*`                    | Le cinque fasi del pattern State con le transizioni consentite.                                                                                                                                           |

### Front-end (`javafx-app`)

| Classe                                                                                  | Responsabilità                                                                                                                                                                                                                                                                                  |
| --------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Launcher` / `Applicazione`                                                             | Avvio dell'app (Launcher separato per evitare l'errore "JavaFX runtime missing").                                                                                                                                                                                                               |
| `SceneManager`                                                                          | Unico gestore della navigazione tra schermate; inietta il contesto nei controller.                                                                                                                                                                                                              |
| `AppContext`                                                                            | Composition root del front-end: motore, scenario, persistenza, navigazione.                                                                                                                                                                                                                     |
| `CreazioneController`, `EsplorazioneController`, `AccusaController`, `FinaleController` | Controller MVC delle rispettive viste FXML; la creazione gestisce la **distribuzione dei punti caratteristica** (con anteprima dello stile), l'esplorazione filtra i dialoghi per **stile**, mostra esiti delle prove ed esperienza, l'HUD con livello/punti e il **menu** (salva/carica/esci). |
| `FinestraTaccuino`, `EffettoTesto`, `RisorseGrafiche`                                   | Taccuino (inclusa la **scheda Investigatore** con spesa dei punti abilità), effetto macchina da scrivere, accesso alle risorse.                                                                                                                                                                 |

## Design pattern adottati

| Pattern                     | Dove                                  | Motivazione                                                                                                   |
| --------------------------- | ------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| **MVC**                     | intera GUI                            | Separa Model (core), View (FXML/CSS) e Controller.                                                            |
| **MVVM / Observer**         | binding JavaFX                        | Le `Property` legano dati e nodi: aggiornamento automatico della vista.                                       |
| **State**                   | `FaseDiGioco` e implementazioni       | Ogni fase gestisce diversamente le azioni; nuove fasi senza toccare le esistenti (OCP).                       |
| **Strategy**                | `ValutatoreAccusa`, `RisolutoreProva` | Regola di vittoria e risoluzione delle prove di abilità intercambiabili (e testabili in modo deterministico). |
| **Factory**                 | `ScenarioLoader`                      | Costruzione del `Caso` dai dati esterni, disaccoppiando creazione e uso.                                      |
| **Repository / DAO**        | `GameStateRepository`                 | Astrae il _come_ si salva (oggi XML, domani JPA/JSON).                                                        |
| **Decorator**               | `BufferedWriter` sul salvataggio      | Scrittura bufferizzata.                                                                                       |
| **Singleton** (con cautela) | istanze `INSTANCE` delle fasi         | Fasi stateless condivise, senza stato globale mutabile.                                                       |

## Principi SOLID

- **SRP** — `Partita` custodisce lo stato, `MotorePartita` orchestra le operazioni, i controller gestiscono la UI, le classi `persistence.xml` la serializzazione: responsabilità nette.
- **OCP** — nuovi casi (XML), nuove fasi (`FaseDiGioco`), nuovi valutatori (`ValutatoreAccusa`), nuovi risolutori di prove (`RisolutoreProva`) e nuove persistenze (`GameStateRepository`) si aggiungono senza modificare il codice esistente.
- **LSP** — `Sospettato`/`Testimone` rispettano il contratto di `Personaggio`; le implementazioni delle interfacce sono sostituibili.
- **ISP** — interfacce piccole e mirate (`Interrogabile`, `Ispezionabile`, ...).
- **DIP** — il dominio e la UI dipendono da astrazioni (`ScenarioLoader`, `GameStateRepository`, `ValutatoreAccusa`), non dalle implementazioni.
