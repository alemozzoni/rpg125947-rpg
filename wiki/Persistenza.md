# Persistenza e Organizzazione dei Dati

Il progetto distingue due tipi di dati, entrambi gestiti in **XML** tramite la libreria standard **JAXP** del JDK (nessuna dipendenza esterna).

## 1. Dati statici dello scenario

La definizione del caso (stanze, personaggi, dialoghi, indizi e soluzione) vive in `core/src/main/resources/.../scenario/caso.xml`, **separata dal codice**. Al caricamento:

1. il documento viene **validato** contro lo schema `scenario.xsd` durante il parsing (`DocumentBuilderFactory.setSchema`); un `ErrorHandler` trasforma gli errori in eccezioni, così uno scenario non conforme viene **rifiutato**;
2. il DOM risultante viene **navigato con XPath** (`/caso/stanze/stanza`, `/caso/indizi/indizio`, `/caso/soluzione`, ...) e con traversamento DOM dei figli, per costruire l'aggregato `Caso`.

Lo schema XSD vincola tipi, attributi obbligatori, enumerazioni (`TipoIndizio`, `Direzione`, `Attributo`) e l'intervallo `[0,1]` delle coordinate. Il `CaricatoreCasoXml` controlla inoltre l'integrità referenziale (ogni `indizio` riferito esiste, il colpevole è tra i sospettati).

Lo scenario è inoltre estendibile con le **prove di abilità** ([Sistema di Ruolo](Sistema-di-Ruolo)): un `hotspot` o un'`opzione` di dialogo possono dichiarare gli attributi opzionali `attributo` (enum `Attributo`) e `difficolta` (intero positivo). Un'`opzione` può inoltre dichiarare l'attributo opzionale `stile` (enum `Attributo`) che la riserva a un determinato **stile investigativo**; in sua assenza la domanda è universale. Tutti questi attributi sono facoltativi: omettendoli l'interazione riesce automaticamente ed è visibile a chiunque, garantendo la **retrocompatibilità** degli scenari preesistenti.

```xml
<hotspot id="teca" nome="Teca del Manoscritto" x="0.32" y="0.45"
         indizio="anacronismo" attributo="OSSERVAZIONE" difficolta="12">...</hotspot>
```

**Conseguenza progettuale (OCP):** aggiungere o modificare un caso **non richiede ricompilare il codice**, basta un nuovo file XML conforme allo schema. Il `ScenarioLoader` è l'unico punto che conosce il formato.

```java
SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
f.setNamespaceAware(true);
f.setSchema(sf.newSchema(new StreamSource(xsd)));   // il caso è VALIDO solo se rispetta lo schema
Document doc = f.newDocumentBuilder().parse(xml);   // DOM
XPath xpath = XPathFactory.newInstance().newXPath();
NodeList stanze = (NodeList) xpath.evaluate("/caso/stanze/stanza", doc, XPathConstants.NODESET);
```

## 2. Stato della partita (salvataggio/caricamento)

`XmlGameStateRepository` salva la partita su file costruendo un albero **DOM** e serializzandolo con un `Transformer` su uno stream **bufferizzato** (`BufferedWriter`, pattern Decorator), entro un _try-with-resources_. Sono persistiti l'investigatore con la sua **scheda personaggio** (livello, esperienza, punti abilità e valori degli attributi), la stanza corrente, gli indizi raccolti, i sospettati annotati e gli appunti.

Al **caricamento** il DOM del salvataggio viene letto, la scheda dell'investigatore viene **ricostruita** (attributi e progressione) e gli indizi (salvati per `id`) vengono **ricollegati** alle entità del `Caso` corrente. I salvataggi privi di scheda (formati precedenti) ricadono sul profilo standard: il caricamento resta **retrocompatibile**. I file risiedono nella cartella `salvataggi/` (esclusa dal versionamento).

```xml
<partita investigatore="Sherlock" stanza="sala_server"
         livello="2" esperienza="10" puntiAbilita="1">
  <attributi>
    <attributo nome="OSSERVAZIONE" valore="5"/>
    <attributo nome="INTUITO" valore="3"/>
    <attributo nome="ELOQUENZA" valore="2"/>
    <attributo nome="LOGICA" valore="4"/>
  </attributi>
  <taccuino>
    <indizio id="anacronismo"/>
    <sospettato nome="Beatrice Lovato"/>
    <appunto>Controllare l'orario del badge</appunto>
  </taccuino>
</partita>
```

## Strutture dati di dominio

- **`Set<Indizio>`** nel `Taccuino`: niente duplicati, `add`/`contains` in tempo costante medio. Richiede `equals`/`hashCode` su `Indizio` (identità sull'`id`).
- **`Map<String, List<Testimonianza>>`** per fonte: recupero diretto delle dichiarazioni di un personaggio. L'inserimento **scarta i duplicati** (`Testimonianza` è un `record`, con `equals` per valore): ri-porre la stessa domanda non sporca il taccuino.
- **`Map<String, Stanza>`** e **`Map<String, Indizio>`** nel `Caso`: lookup per id durante caricamento e gioco.

## Affidabilità

La persistenza è coperta da test JUnit: round-trip salva→carica della partita e **rifiuto di uno scenario XML non valido** da parte della validazione XSD. Le meccaniche di ruolo hanno test dedicati: progressione dell'`Investigatore` (esperienza, livelli, spesa dei punti), skill check con risolutori deterministici (prova fallita/superata, esperienza non duplicata) e non duplicazione delle testimonianze nel `Taccuino`.
