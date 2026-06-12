package it.unicam.cs.mpgc.rpg125947.controller;

import it.unicam.cs.mpgc.rpg125947.app.AppContext;
import it.unicam.cs.mpgc.rpg125947.app.RisorseGrafiche;
import it.unicam.cs.mpgc.rpg125947.app.ui.EffettoTesto;
import it.unicam.cs.mpgc.rpg125947.app.ui.FinestraTaccuino;
import it.unicam.cs.mpgc.rpg125947.logic.MotorePartita;
import it.unicam.cs.mpgc.rpg125947.logic.RisultatoInterazione;
import it.unicam.cs.mpgc.rpg125947.model.Attributo;
import it.unicam.cs.mpgc.rpg125947.model.AzioneGiocatore;
import it.unicam.cs.mpgc.rpg125947.model.Coordinata;
import it.unicam.cs.mpgc.rpg125947.model.Hotspot;
import it.unicam.cs.mpgc.rpg125947.model.Investigatore;
import it.unicam.cs.mpgc.rpg125947.model.Partita;
import it.unicam.cs.mpgc.rpg125947.model.Stanza;
import it.unicam.cs.mpgc.rpg125947.model.Uscita;
import it.unicam.cs.mpgc.rpg125947.model.dialogo.Dialogo;
import it.unicam.cs.mpgc.rpg125947.model.dialogo.OpzioneDialogo;
import it.unicam.cs.mpgc.rpg125947.model.personaggio.Personaggio;
import it.unicam.cs.mpgc.rpg125947.model.personaggio.Sospettato;
import it.unicam.cs.mpgc.rpg125947.model.prova.EsitoProva;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

/**
 * Controller della scena di esplorazione: rende graficamente la stanza corrente
 * (sfondo, sprite dei personaggi, hotspot), gestisce navigazione, dialoghi,
 * ispezioni, salvataggio/caricamento e l'avvio dell'accusa.
 *
 * <p>Coordina la View con il dominio attraverso il {@link MotorePartita}: ogni
 * interazione si traduce in un'azione di gioco (pattern State) e in un
 * aggiornamento della vista (Observer/MVC).</p>
 */
public final class EsplorazioneController {

    private static final double SPRITE_LARGHEZZA = 150;
    private static final double SPRITE_ALTEZZA = 210;
    private static final double HOTSPOT_LATO = 88;

    /** Gli slot di salvataggio disponibili (id di persistenza). */
    private static final List<String> SLOT = List.of("slot1", "slot2", "slot3");

    private final AppContext context;

    @FXML private StackPane radice;
    @FXML private ImageView sfondo;
    @FXML private Pane stratoHotspot;
    @FXML private Pane stratoPersonaggi;
    @FXML private BorderPane hud;
    @FXML private Label nomeStanza;
    @FXML private Label notifica;
    @FXML private Label statoInvestigatore;
    @FXML private HBox usciteNord;
    @FXML private HBox usciteSud;
    @FXML private VBox usciteEst;
    @FXML private VBox usciteOvest;
    @FXML private StackPane overlay;

    public EsplorazioneController(AppContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        sfondo.fitWidthProperty().bind(radice.widthProperty());
        sfondo.fitHeightProperty().bind(radice.heightProperty());
        mostraStanza(partita().getStanzaCorrente());
        aggiornaStato();
    }

    /** Aggiorna l'indicatore in basso a sinistra: livello e punti abilita disponibili. */
    private void aggiornaStato() {
        Investigatore inv = partita().getInvestigatore();
        statoInvestigatore.setText(
                "Livello " + inv.getLivello() + "   ·   Punti abilita: " + inv.getPuntiAbilita());
    }

    // ===== Rendering della stanza =====

    private void mostraStanza(Stanza stanza) {
        sfondo.setImage(RisorseGrafiche.immagine(stanza.getSfondoPath()));
        nomeStanza.setText(stanza.getNome());
        stratoPersonaggi.getChildren().clear();
        stratoHotspot.getChildren().clear();
        usciteNord.getChildren().clear();
        usciteSud.getChildren().clear();
        usciteEst.getChildren().clear();
        usciteOvest.getChildren().clear();

        stanza.getHotspot().forEach(this::aggiungiHotspot);
        stanza.getPersonaggi().forEach(this::aggiungiSprite);
        stanza.getUscite().forEach(this::aggiungiUscita);
    }

    private void aggiungiSprite(Personaggio personaggio) {
        // La scala per-personaggio (dallo scenario) ingrandisce il singolo sprite.
        double larghezza = SPRITE_LARGHEZZA * personaggio.getScala();
        double altezza = SPRITE_ALTEZZA * personaggio.getScala();

        ImageView iv = new ImageView(RisorseGrafiche.immagine(personaggio.getSpritePath()));
        iv.setFitWidth(larghezza);
        iv.setPreserveRatio(true);

        Label nome = new Label(personaggio.getNome());
        nome.getStyleClass().add("nome-personaggio");

        VBox sprite = new VBox(4, iv, nome);
        sprite.setAlignment(Pos.CENTER);
        sprite.getStyleClass().add("sprite-personaggio");
        sprite.setCursor(Cursor.HAND);
        sprite.setOnMouseClicked(e -> apriDialogo(personaggio));
        posiziona(sprite, personaggio.getPosizione(), larghezza, altezza);
        stratoPersonaggi.getChildren().add(sprite);
    }

    private void aggiungiHotspot(Hotspot hotspot) {
        Button area = new Button();
        area.getStyleClass().add("hotspot");
        area.setPrefSize(HOTSPOT_LATO, HOTSPOT_LATO);
        area.setCursor(Cursor.HAND);
        area.setTooltip(new Tooltip(hotspot.getNome()));
        // Icona-lente sempre visibile: segnala l'oggetto interagibile anche su
        // sfondi astratti, senza dipendere dai font di sistema.
        ImageView icona = new ImageView(RisorseGrafiche.immagine("lente.png"));
        icona.setFitWidth(36);
        icona.setPreserveRatio(true);
        area.setGraphic(icona);
        area.setOnAction(e -> ispeziona(hotspot));
        posiziona(area, hotspot.getPosizione(), HOTSPOT_LATO, HOTSPOT_LATO);
        stratoHotspot.getChildren().add(area);
    }

    private void aggiungiUscita(Uscita uscita) {
        Button bottone = new Button(frecciaDi(uscita) + " " + uscita.etichetta());
        bottone.getStyleClass().add("bottone-uscita");
        bottone.setOnAction(e -> vaiA(uscita));
        switch (uscita.direzione()) {
            case NORD -> usciteNord.getChildren().add(bottone);
            case SUD -> usciteSud.getChildren().add(bottone);
            case EST -> usciteEst.getChildren().add(bottone);
            case OVEST -> usciteOvest.getChildren().add(bottone);
        }
    }

    private String frecciaDi(Uscita uscita) {
        return switch (uscita.direzione()) {
            case NORD -> "▲";
            case SUD -> "▼";
            case EST -> "▶";
            case OVEST -> "◀";
        };
    }

    /** Ancora il nodo a una coordinata relativa, centrandolo, in modo responsivo. */
    private void posiziona(Region nodo, Coordinata coord, double larghezza, double altezza) {
        nodo.layoutXProperty().bind(radice.widthProperty().multiply(coord.x()).subtract(larghezza / 2));
        nodo.layoutYProperty().bind(radice.heightProperty().multiply(coord.y()).subtract(altezza / 2));
    }

    // ===== Interazioni =====

    private void vaiA(Uscita uscita) {
        Stanza destinazione = context.getCaso().getStanza(uscita.idStanzaDestinazione());
        if (motore().muovi(destinazione)) {
            mostraStanza(destinazione);
        } else {
            mostraMessaggio("Porta chiusa",
                    "Questa stanza e chiusa a chiave. Devi prima procurarti la chiave giusta.");
        }
    }

    private void apriDialogo(Personaggio personaggio) {
        motore().eseguiAzione(AzioneGiocatore.ENTRA_IN_DIALOGO);
        if (personaggio instanceof Sospettato sospettato) {
            motore().annota(sospettato);
        }
        Dialogo dialogo = personaggio.avviaDialogo();

        Label battuta = new Label();
        battuta.getStyleClass().add("testo-dialogo");
        battuta.setWrapText(true);
        battuta.setMaxWidth(720);
        Label feedback = new Label();
        feedback.getStyleClass().add("feedback-indizio");

        ImageView ritratto = new ImageView(RisorseGrafiche.immagine(personaggio.getSpritePath()));
        ritratto.setFitWidth(96);
        ritratto.setPreserveRatio(true);
        Label intestazione = new Label(personaggio.getNome() + "\n" + personaggio.getRuolo());
        intestazione.getStyleClass().add("nome-dialogo");
        HBox testa = new HBox(14, ritratto, intestazione);
        testa.setAlignment(Pos.CENTER_LEFT);

        // Le domande disponibili dipendono dallo stile investigativo: l'attributo
        // su cui il giocatore ha investito piu punti sblocca battute dedicate.
        Attributo stile = partita().getInvestigatore().attributoDominante();
        VBox domande = new VBox(8);
        for (OpzioneDialogo opzione : dialogo.opzioniPer(stile)) {
            // Le domande che sono una prova di abilita mostrano l'attributo richiesto;
            // quelle dedicate allo stile sono evidenziate con un contrassegno.
            String etichetta = opzione.richiedeProva()
                    ? "🎲 [" + opzione.attributoRichiesto().orElseThrow().etichetta() + "] " + opzione.domanda()
                    : opzione.domanda();
            if (opzione.stileRichiesto().isPresent()) {
                etichetta = "✦ " + etichetta;
            }
            domande.getChildren().add(bottone(etichetta, "domanda-dialogo",
                    () -> rispondi(opzione, battuta, feedback)));
        }

        VBox pannello = new VBox(12, testa, battuta, domande, feedback,
                bottone("Chiudi", "bottone-chiudi", this::chiudiOverlay));
        pannello.getStyleClass().add("pannello-dialogo");
        pannello.setMaxWidth(820);
        pannello.setMaxHeight(Region.USE_PREF_SIZE);
        mostraOverlay(pannello, Pos.BOTTOM_CENTER, new Insets(0, 24, 24, 24));
        EffettoTesto.scrivi(battuta, dialogo.getBattutaIniziale());
    }

    private void rispondi(OpzioneDialogo opzione, Label battuta, Label feedback) {
        RisultatoInterazione esito = motore().interroga(opzione);
        if (esito.provaFallita()) {
            // Prova non superata: il personaggio non rivela l'informazione.
            EffettoTesto.scrivi(battuta, "Mmh... preferirei non parlarne. Mi dispiace.");
            feedback.setText(componiFeedback(esito));
            return;
        }
        EffettoTesto.scrivi(battuta, opzione.risposta().testo());
        feedback.setText(componiFeedback(esito));
        if (esito.salitoDiLivello()) {
            notificaLivello();
        }
    }

    private void ispeziona(Hotspot hotspot) {
        motore().eseguiAzione(AzioneGiocatore.ISPEZIONA);
        RisultatoInterazione esito = motore().ispeziona(hotspot);

        Label titolo = new Label(hotspot.getNome());
        titolo.getStyleClass().add("titolo-ispezione");
        Label descrizione = new Label();
        descrizione.getStyleClass().add("testo-ispezione");
        descrizione.setWrapText(true);
        descrizione.setMaxWidth(620);

        VBox pannello = new VBox(12, titolo, descrizione);
        pannello.getStyleClass().add("pannello-ispezione");
        pannello.setMaxWidth(680);
        pannello.setMaxHeight(Region.USE_PREF_SIZE);

        String feedback = componiFeedback(esito);
        if (!feedback.isBlank()) {
            Label fb = new Label(feedback);
            fb.getStyleClass().add(esito.provaFallita() ? "feedback-prova-fallita" : "feedback-indizio");
            fb.setWrapText(true);
            pannello.getChildren().add(fb);
        }
        pannello.getChildren().add(bottone("Chiudi", "bottone-chiudi", this::chiudiOverlay));
        mostraOverlay(pannello, Pos.CENTER, Insets.EMPTY);
        EffettoTesto.scrivi(descrizione, hotspot.getDescrizione());
        if (esito.salitoDiLivello()) {
            notificaLivello();
        }
    }

    /**
     * Compone il riscontro testuale di un'interazione: esito dell'eventuale prova,
     * indizio scoperto ed esperienza guadagnata.
     */
    private String componiFeedback(RisultatoInterazione esito) {
        StringBuilder sb = new StringBuilder();
        esito.provaTentata().ifPresent(prova -> sb.append(descriviProva(prova)).append('\n'));
        esito.indizioScoperto().ifPresent(indizio -> sb
                .append("Indizio aggiunto al taccuino: ").append(indizio.getNome())
                .append("  (+").append(esito.xpGuadagnati()).append(" PE)"));
        if (esito.provaFallita()) {
            sb.append("Non emerge nulla di utile: puoi riprovare quando sarai piu esperto.");
        }
        return sb.toString().trim();
    }

    private String descriviProva(EsitoProva prova) {
        return "Prova di " + prova.attributo().etichetta() + ": "
                + (prova.superata() ? "superata" : "fallita")
                + " (" + prova.totale() + " contro " + prova.difficolta() + ")";
    }

    /** Notifica il passaggio di livello e invita a spendere i punti nel taccuino. */
    private void notificaLivello() {
        int livello = partita().getInvestigatore().getLivello();
        notificaBreve("Livello " + livello + " raggiunto! Nuovi punti abilita da spendere nel Taccuino.");
        aggiornaStato();
    }

    private void mostraMessaggio(String titolo, String testo) {
        Label t = new Label(titolo);
        t.getStyleClass().add("titolo-ispezione");
        Label corpo = new Label(testo);
        corpo.getStyleClass().add("testo-ispezione");
        corpo.setWrapText(true);
        corpo.setMaxWidth(520);
        VBox pannello = new VBox(12, t, corpo, bottone("Chiudi", "bottone-chiudi", this::chiudiOverlay));
        pannello.getStyleClass().add("pannello-ispezione");
        pannello.setMaxWidth(560);
        pannello.setMaxHeight(Region.USE_PREF_SIZE);
        mostraOverlay(pannello, Pos.CENTER, Insets.EMPTY);
    }

    // ===== Comandi HUD =====

    @FXML
    private void onTaccuino() {
        new FinestraTaccuino().mostra(finestra(), partita());
        // Nel taccuino si possono spendere punti abilita: riallinea l'indicatore.
        aggiornaStato();
    }

    /** Apre il menu di gioco: salva, carica o esci al menu principale. */
    @FXML
    private void onMenu() {
        VBox pannello = pannelloSlot("Menu", "Cosa vuoi fare?");
        pannello.setAlignment(Pos.CENTER);
        pannello.getChildren().addAll(
                bottone("Salva partita", "domanda-dialogo", this::onSalva),
                bottone("Carica partita", "domanda-dialogo", this::onCarica),
                bottone("Esci al menu principale", "domanda-dialogo", this::onEsci),
                bottone("Annulla", "bottone-chiudi", this::chiudiOverlay));
        mostraOverlay(pannello, Pos.CENTER, Insets.EMPTY);
    }

    /** Chiede conferma e, se accordata, riporta alla schermata principale. */
    private void onEsci() {
        VBox pannello = pannelloSlot("Esci dalla partita",
                "Tornerai al menu principale. I progressi non salvati andranno persi.");
        pannello.setAlignment(Pos.CENTER);
        pannello.getChildren().addAll(
                bottone("Esci senza salvare", "bottone-accusa", this::tornaAlMenuPrincipale),
                bottone("Annulla", "bottone-chiudi", this::onMenu));
        mostraOverlay(pannello, Pos.CENTER, Insets.EMPTY);
    }

    /** Chiude l'overlay e naviga alla schermata di creazione (pagina principale). */
    private void tornaAlMenuPrincipale() {
        chiudiOverlay();
        context.getSceneManager().mostraCreazione();
    }

    private void onSalva() {
        List<String> occupati = context.getRepository().slotDisponibili();
        VBox pannello = pannelloSlot("Salva partita", "Scegli in quale slot salvare:");
        for (String slot : SLOT) {
            String stato = occupati.contains(slot) ? " (sovrascrivi)" : " (vuoto)";
            pannello.getChildren().add(bottone(etichettaSlot(slot) + stato, "domanda-dialogo",
                    () -> salvaSu(slot)));
        }
        pannello.getChildren().add(bottone("Annulla", "bottone-chiudi", this::chiudiOverlay));
        mostraOverlay(pannello, Pos.CENTER, Insets.EMPTY);
    }

    private void salvaSu(String slot) {
        chiudiOverlay();
        try {
            context.getRepository().salva(partita(), slot);
            notificaBreve(etichettaSlot(slot) + " salvato.");
        } catch (RuntimeException e) {
            notificaBreve("Salvataggio non riuscito.");
        }
    }

    private void onCarica() {
        List<String> occupati = context.getRepository().slotDisponibili();
        VBox pannello = pannelloSlot("Carica partita", "Scegli quale partita riprendere:");
        for (String slot : SLOT) {
            boolean disponibile = occupati.contains(slot);
            Button b = bottone(etichettaSlot(slot) + (disponibile ? "" : " (vuoto)"),
                    "domanda-dialogo", () -> caricaDa(slot));
            b.setDisable(!disponibile); // non si carica uno slot vuoto
            pannello.getChildren().add(b);
        }
        pannello.getChildren().add(bottone("Annulla", "bottone-chiudi", this::chiudiOverlay));
        mostraOverlay(pannello, Pos.CENTER, Insets.EMPTY);
    }

    private void caricaDa(String slot) {
        chiudiOverlay();
        Optional<Partita> caricata = context.getRepository().carica(slot, context.getCaso());
        if (caricata.isPresent()) {
            motore().riprendi(caricata.get());
            mostraStanza(caricata.get().getStanzaCorrente());
            aggiornaStato();
            notificaBreve(etichettaSlot(slot) + " caricato.");
        } else {
            notificaBreve("Nessun salvataggio in " + etichettaSlot(slot) + ".");
        }
    }

    /** Etichetta leggibile di uno slot: {@code "slot2"} -> {@code "Slot 2"}. */
    private static String etichettaSlot(String slot) {
        return "Slot " + slot.substring("slot".length());
    }

    /** Pannello-overlay base con titolo e sottotitolo per la scelta dello slot. */
    private VBox pannelloSlot(String titolo, String sottotitolo) {
        Label t = new Label(titolo);
        t.getStyleClass().add("titolo-ispezione");
        Label s = new Label(sottotitolo);
        s.getStyleClass().add("testo-ispezione");
        s.setWrapText(true);
        s.setMaxWidth(520);
        VBox pannello = new VBox(12, t, s);
        pannello.getStyleClass().add("pannello-ispezione");
        pannello.setMaxWidth(560);
        pannello.setMaxHeight(Region.USE_PREF_SIZE);
        return pannello;
    }

    @FXML
    private void onAccusa() {
        motore().eseguiAzione(AzioneGiocatore.APRI_ACCUSA);
        context.getSceneManager().mostraAccusa();
    }

    // ===== Utility =====

    private void mostraOverlay(Node contenuto, Pos posizione, Insets margine) {
        overlay.getChildren().setAll(contenuto);
        StackPane.setAlignment(contenuto, posizione);
        StackPane.setMargin(contenuto, margine);
        overlay.setVisible(true);
        overlay.setManaged(true);
    }

    private void chiudiOverlay() {
        overlay.getChildren().clear();
        overlay.setVisible(false);
        overlay.setManaged(false);
        motore().eseguiAzione(AzioneGiocatore.CHIUDI_INTERAZIONE);
    }

    private Button bottone(String testo, String styleClass, Runnable azione) {
        Button b = new Button(testo);
        b.getStyleClass().add(styleClass);
        b.setOnAction(e -> azione.run());
        return b;
    }

    private void notificaBreve(String messaggio) {
        notifica.setText(messaggio);
        PauseTransition pausa = new PauseTransition(Duration.seconds(2.5));
        pausa.setOnFinished(e -> notifica.setText(""));
        pausa.play();
    }

    private Window finestra() {
        return radice.getScene().getWindow();
    }

    private MotorePartita motore() {
        return context.getMotore();
    }

    private Partita partita() {
        return context.getMotore().getPartita();
    }
}
