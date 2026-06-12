# 🕵️ Delitto al Museo della Computazione

Gioco di ruolo investigativo (**murder mystery RPG**) con interfaccia grafica, scritto in **Java 25** con **JavaFX**.
Il giocatore veste i panni di un investigatore che deve risolvere un omicidio avvenuto durante l'inaugurazione notturna di un museo dedicato alla storia dell'informatica: crea il proprio personaggio distribuendo i punti caratteristica, esplora le sale, interroga i sospettati superando **prove di abilità**, raccoglie indizi guadagnando **esperienza** e formula l'accusa finale.

Progetto d'esame per il corso di **Metodologie di Programmazione e Modellazione della Conoscenza** — Università degli Studi di Camerino, A.A. 2025/2026.

---

## 🚀 Come eseguire il progetto

### Prerequisiti

- **Java 25** (JDK). Non è necessario installare Gradle né JavaFX a mano: il **Gradle Wrapper** scarica la versione corretta di Gradle e il plugin JavaFX risolve automaticamente le librerie grafiche. La toolchain Java 25, se assente, viene scaricata automaticamente.

### Istruzioni

```bash
git clone https://github.com/alemozzoni/rpg125947.git
cd rpg125947
```

### Build del progetto

```bash
./gradlew build
```

### Esecuzione

```bash
./gradlew run
```

> Due soli comandi bastano per compilare ed eseguire, come da specifica di consegna.

---

## 🎮 Come si gioca

1. All'avvio inserisci il **nome dell'investigatore** e **distribuisci i punti caratteristica** tra le quattro abilità (Osservazione, Intuito, Eloquenza, Logica), poi premi _Inizia a Giocare_. L'abilità su cui investi di più definisce il tuo **stile investigativo**, anticipato in tempo reale sotto la scheda.
2. **Esplora** le sale del museo usando le frecce/uscite sul bordo della scena.
3. **Clicca sui personaggi** per interrogarli: le testimonianze finiscono nel taccuino. Alcune domande sono **prove di Eloquenza** (🎲): solo superandole il testimone parla. Le domande proposte **cambiano con il tuo stile investigativo**: profili diversi pongono domande diverse e ottengono risposte diverse, pur potendo raggiungere tutti la **stessa soluzione**. Le domande contrassegnate da **✦** sono quelle sbloccate dal tuo stile dominante.
4. **Clicca sugli hotspot** (aree che si illuminano al passaggio del mouse) per ispezionare oggetti. Gli indizi più nascosti richiedono una **prova di abilità** (Osservazione, Intuito o Logica): un tiro di dado **d20 + attributo** contro una difficoltà.
5. Ogni indizio raccolto dà **esperienza**: salendo di livello ottieni **punti abilità** da investire sugli attributi dalla scheda _Investigatore_ del Taccuino. **Livello** e **punti abilità disponibili** sono sempre visibili in basso a sinistra.
6. Consulta il **Taccuino** per rivedere scheda personaggio, indizi, testimonianze e sospettati. Dal pulsante **Menu** puoi **salvare**, **caricare** (progressione inclusa) o **uscire** al menu principale.
7. Quando hai le prove, premi **Formula accusa**, indica il colpevole e seleziona gli indizi a supporto. L'accusa è corretta solo con il colpevole giusto **e** tutte le prove decisive.

---

## 🎲 Elementi di ruolo (RPG)

- **Scheda personaggio**: quattro attributi (Osservazione, Intuito, Eloquenza, Logica) scelti in creazione distribuendo un pool di punti.
- **Stile investigativo**: l'attributo dominante (quello su cui hai messo più punti) **ramifica i dialoghi**. Ogni interlocutore offre domande _universali_ (uguali per tutti, incluse quelle decisive) e domande _dedicate_ a uno specifico stile, con risposte caratterizzate. Le prove decisive restano raggiungibili da qualunque profilo: la **soluzione è unica**, il percorso narrativo no. Lo stile è derivato dagli attributi, quindi evolve anche salendo di livello e spendendo punti nel taccuino.

  | Stile dominante  | Taglio degli interrogatori                                               |
  | ---------------- | ------------------------------------------------------------------------ |
  | **Osservazione** | nota dettagli fisici e incongruenze (oggetti mancanti, registri, calici) |
  | **Intuito**      | fiuta disagio e moventi, punta dritto a chi ha più da perdere            |
  | **Eloquenza**    | mette a proprio agio l'interlocutore e ne ottiene confidenze             |
  | **Logica**       | ricostruisce orari, alibi e catene di prova in modo metodico             |

- **Prove di abilità** (_skill check_): ispezioni e interrogatori chiave si risolvono con un tiro **d20 + attributo ≥ difficoltà**. La logica è una _Strategy_ (`RisolutoreProva`) iniettabile, quindi i test la rendono deterministica.
- **Progressione**: raccogliere indizi accredita esperienza; al passaggio di livello si guadagnano punti abilità da spendere sugli attributi. Livello, esperienza e attributi sono **persistiti** nel salvataggio.

---

## 🏗️ Struttura del progetto

Build **Gradle multi-progetto** che separa il dominio dalla presentazione (predisposizione multi-dispositivo):

| Modulo       | Responsabilità                                                                                                                                                                                              |
| ------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `core`       | Modello di dominio, logica di gioco e persistenza astratta. **Platform-independent**: non dipende da JavaFX. Persistenza in **XML** (JAXP: DOM/SAX, validazione **XSD**, query **XPath**), inclusa nel JDK. |
| `javafx-app` | Front-end desktop **JavaFX** (FXML + CSS + binding), dipende da `core`.                                                                                                                                     |

Tutto il codice è nel package `it.unicam.cs.mpgc.rpg125947`.

La documentazione completa (funzionalità, responsabilità delle classi e interfacce, organizzazione dei dati e persistenza, meccanismi di estensione) è nella **[Wiki del repository](https://github.com/alemozzoni/rpg125947/wiki)**; le stesse pagine sono versionate nella cartella [`wiki/`](wiki/).

---

## 🤖 Uso di strumenti di AI

Questo progetto è stato realizzato **con un uso mirato di Intelligenza Artificiale**, in particolare **Claude (Anthropic)**, utilizzato per consigli e stesura di alcune righe di codice per semplificare il lavoro.

**Per quali attività è stata usata l'AI:**

- generazione dello scheletro del progetto (build Gradle multi-progetto, wrapper, configurazione JavaFX/JUnit);
- aiuto nella scrittura del codice del dominio, della logica di gioco, della persistenza XML e dei controller JavaFX, **a partire dal documento di progettazione** (trama, architettura, pattern e scelte tecnologiche definiti a monte);
- controllato e revisionato i testi del caso (dialoghi, indizi, soluzione) e della documentazione (README e Wiki);
- generazione degli asset grafici procedurali tramite uno script ImageMagick.

**Livello di intervento e supervisione personale:**

- le **scelte di progettazione** (architettura, pattern, stack, trama e meccaniche) sono state definite e validate consapevolmente;
- ogni componente è stato **compreso, verificato ed eseguito**: il progetto compila con `./gradlew build`, supera la suite di **test JUnit** ed è stato avviato e provato graficamente;
- l'AI è stata usata come **supporto** alla scrittura e all'organizzazione del codice, non come sostituto della comprensione.

Una **dichiarazione dettagliata** dell'uso dell'AI è disponibile nella pagina dedicata della Wiki.
