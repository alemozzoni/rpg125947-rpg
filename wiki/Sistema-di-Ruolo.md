# Sistema di Ruolo (RPG)

Oltre all'indagine, il gioco implementa le meccaniche tipiche di un **gioco di ruolo**: una scheda personaggio con caratteristiche, prove di abilità risolte col dado e progressione per esperienza e livelli. Questa pagina descrive le tre meccaniche e il loro design.

## 1. Scheda personaggio e caratteristiche

L'investigatore non è più una semplice etichetta col nome: possiede una **scheda** con quattro caratteristiche (`Attributo`), pensate per coprire i diversi approcci all'indagine:

| Attributo        | Significato investigativo                     |
| ---------------- | --------------------------------------------- |
| **Osservazione** | notare dettagli, anacronismi e tracce fisiche |
| **Intuito**      | cogliere moventi e collegamenti non evidenti  |
| **Eloquenza**    | convincere i testimoni reticenti a parlare    |
| **Logica**       | ricostruire cronologie e smontare alibi       |

In fase di **creazione del personaggio** il giocatore distribuisce un pool di punti (`Investigatore.PUNTI_CREAZIONE`) sulle quattro caratteristiche, partendo da un valore base e nel rispetto di un tetto massimo. La build scelta determina in quali prove l'investigatore sarà più forte, dando **rigiocabilità** al caso.

## 2. Prove di abilità (skill check)

Le ispezioni e gli interrogatori più significativi non riescono automaticamente: sono **prove di abilità** risolte con un classico **tiro d20**:

```
tiro (1..20) + valore dell'attributo  ≥  difficoltà   →   prova superata
```

- Un **hotspot** può richiedere una prova per rivelare il proprio indizio (es. la _Teca del Manoscritto_ richiede **Osservazione 12**).
- Un'**opzione di dialogo** può essere una prova (es. convincere la stagista con **Eloquenza 11**): solo superandola il personaggio rivela la testimonianza. In caso di fallimento si può **ritentare**.

La regola è incapsulata nella _Strategy_ **`RisolutoreProva`**, gemella di `ValutatoreAccusa`:

| Implementazione       | Uso                                                       |
| --------------------- | --------------------------------------------------------- |
| `RisolutoreProvaDado` | gioco reale: tiro d20 con sorgente `Random` (iniettabile) |
| `sempreSuperata()`    | factory per test deterministici (la prova riesce sempre)  |
| `sempreFallita()`     | factory per test deterministici (la prova fallisce)       |

Iniettare la strategia nel `MotorePartita` rende la logica di gioco **collaudabile senza casualità**: i test verificano separatamente che una prova fallita non riveli l'indizio e che una superata lo annoti e dia esperienza.

L'esito viene confezionato in due value object:

- **`EsitoProva`** — attributo, tiro, valore, difficoltà, superata (con `totale()`): tutto ciò che serve alla UI per mostrare il calcolo in modo trasparente, come a un tavolo da gioco di ruolo.
- **`RisultatoInterazione`** — l'esito ricco di un'ispezione o interrogatorio: prova tentata, indizio scoperto, esperienza guadagnata, eventuale passaggio di livello.

## 3. Progressione: esperienza e livelli

Ogni **nuovo indizio** aggiunto al taccuino accredita esperienza (`MotorePartita.XP_PER_INDIZIO`). Raggiunta la soglia (crescente col livello), l'investigatore **sale di livello** e guadagna **punti abilità** spendibili per aumentare le caratteristiche, dalla scheda _Investigatore_ del taccuino.

```
esperienzaProssimoLivello(livello) = 30 × livello
ad ogni livello:  +PUNTI_PER_LIVELLO punti abilità
```

La progressione (livello, esperienza, punti, valori degli attributi) è **persistita** nel salvataggio: ricaricando una partita si ritrova il personaggio esattamente come lo si era lasciato.

## 4. Stile investigativo (dialoghi ramificati)

La distribuzione dei punti non incide solo sulle prove: definisce lo **stile investigativo** del personaggio, cioè il suo **attributo dominante** (quello col valore più alto; a parità vince il primo nell'ordine dell'enum, scelta deterministica). Lo stile **ramifica i dialoghi**:

- ogni opzione di dialogo può dichiarare un attributo `stile` (campo `OpzioneDialogo.stile`, opzionale): se valorizzato, la domanda compare **solo** all'investigatore con quello stile dominante; se assente, la domanda è **universale** (mostrata a chiunque);
- il `Dialogo` espone `opzioniPer(stileDominante)`, che restituisce le opzioni universali **più** quelle riservate a quello stile;
- nella UI le domande dedicate sono contrassegnate da **✦**.

Il principio guida è: **profili diversi vivono conversazioni diverse, ma tutti possono risolvere il caso**. Gli indizi decisivi provengono dagli _hotspot_ (raggiungibili da qualunque build) e le domande essenziali (es. la chiave dell'ufficio) restano universali, quindi la **soluzione è unica**, il percorso narrativo no. Poiché lo stile è derivato dagli attributi, **evolve** anche salendo di livello e spendendo punti.

Lo stile è calcolato in un solo punto del dominio — `Investigatore.attributoDominante()`, con la variante statica riusabile `Investigatore.dominante(Map)` usata anche per l'**anteprima** in fase di creazione — così la logica non è duplicata.

```
attributoDominante = l'Attributo con il valore più alto della scheda
opzioniPer(stile)  = opzioni universali  +  opzioni il cui stile == stile dominante
```

## Riepilogo del design

| Elemento               | Tipo                 | Ruolo                                                                                     |
| ---------------------- | -------------------- | ----------------------------------------------------------------------------------------- |
| `Attributo`            | enum (`model`)       | le quattro caratteristiche                                                                |
| `Investigatore`        | entità (`model`)     | scheda personaggio: attributi, livello, esperienza, punti; calcola lo **stile dominante** |
| `OpzioneDialogo`       | record (`model`)     | domanda + testimonianza; opzionale **prova** e **stile** riservato                        |
| `Dialogo`              | entità (`model`)     | conversazione; `opzioniPer(stile)` filtra le domande per stile                            |
| `EsitoProva`           | value object         | risultato di un singolo skill check                                                       |
| `RisolutoreProva`      | interfaccia Strategy | come si decide l'esito di una prova                                                       |
| `RisolutoreProvaDado`  | logica               | implementazione a tiro di dado d20                                                        |
| `RisultatoInterazione` | record (`logic`)     | esito ricco di ispezione/interrogatorio (prova + indizio + XP)                            |

Le tre meccaniche poggiano sull'architettura esistente **senza stravolgerla**: nuove caratteristiche del dominio, una Strategy aggiuntiva e l'estensione dichiarativa dello scenario (vedi [Persistenza](Persistenza)).
