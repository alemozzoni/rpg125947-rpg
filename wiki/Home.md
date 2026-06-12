# Delitto al Museo della Computazione — Wiki

Documentazione ufficiale del progetto d'esame di **Metodologie di Programmazione e Modellazione della Conoscenza** (Università di Camerino, A.A. 2025/2026).

_Delitto al Museo della Computazione_ è un **murder mystery RPG** in **Java 25** con interfaccia grafica **JavaFX**: il giocatore crea il proprio investigatore distribuendo le caratteristiche, esplora un museo della storia dell'informatica, interroga i sospettati e ispeziona la scena superando **prove di abilità**, raccoglie indizi guadagnando **esperienza** e formula l'accusa per smascherare l'assassino. La distribuzione dei punti definisce uno **stile investigativo** che ramifica i dialoghi: profili diversi vivono conversazioni diverse pur convergendo sull'unica soluzione.

## Indice della Wiki

1. **[Funzionalità](Funzionalità)** — cosa fa il gioco e come si gioca.
2. **[Sistema di Ruolo](Sistema-di-Ruolo)** — caratteristiche del personaggio, prove di abilità (d20), esperienza e livelli, stile investigativo e dialoghi ramificati.
3. **[Architettura e Responsabilità](Architettura)** — moduli, package, classi e interfacce con le rispettive responsabilità, design pattern e principi SOLID.
4. **[Persistenza e Organizzazione dei Dati](Persistenza)** — scenario in XML validato con XSD, salvataggio della partita, DOM/XPath.
5. **[Estendibilità](Estendibilità)** — come integrare nuove funzionalità (casi, formati di persistenza, dispositivi).
6. **[Dichiarazione di Uso di Strumenti di AI](Dichiarazione-Uso-AI)** — uso dettagliato dell'AI nella realizzazione del progetto.

## Sintesi tecnica

| Aspetto     | Scelta                                                                                                                              |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| Linguaggio  | Java 25                                                                                                                             |
| Build       | Gradle multi-progetto (`core` + `javafx-app`) + Wrapper                                                                             |
| GUI         | JavaFX (FXML + CSS + property binding)                                                                                              |
| Persistenza | XML via JAXP (DOM/SAX, validazione XSD, XPath)                                                                                      |
| Testing     | JUnit 6 (Jupiter)                                                                                                                   |
| Pattern     | MVC, State, Observer, Factory, Strategy (valutazione accusa + prove di abilità), Repository/DAO, Decorator, Singleton (con cautela) |

## Esecuzione

```bash
./gradlew build   # compila e testa
./gradlew run     # avvia il gioco
```

Package radice di tutte le classi: `it.unicam.cs.mpgc.rpg125947`.
