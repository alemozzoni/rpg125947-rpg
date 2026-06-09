package it.unicam.cs.mpgc.rpg125947.app.ui;

import it.unicam.cs.mpgc.rpg125947.app.RisorseGrafiche;
import it.unicam.cs.mpgc.rpg125947.model.Attributo;
import it.unicam.cs.mpgc.rpg125947.model.Indizio;
import it.unicam.cs.mpgc.rpg125947.model.Investigatore;
import it.unicam.cs.mpgc.rpg125947.model.Partita;
import it.unicam.cs.mpgc.rpg125947.model.Taccuino;
import it.unicam.cs.mpgc.rpg125947.model.dialogo.Testimonianza;
import it.unicam.cs.mpgc.rpg125947.model.personaggio.Sospettato;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Finestra modale del taccuino dell'investigatore, organizzata a schede
 * (indizi, testimonianze, sospettati, appunti). E una <strong>vista in sola
 * lettura</strong> sul modello: l'unica modifica consentita e l'aggiunta di
 * appunti liberi.
 */
public final class FinestraTaccuino {

    /** Apre la finestra del taccuino come dialogo modale. */
    public void mostra(Window proprietario, Partita partita) {
        Taccuino taccuino = partita.getTaccuino();

        TabPane schede = new TabPane();
        schede.getTabs().addAll(
                scheda("Investigatore", schedaInvestigatore(partita)),
                scheda("Indizi", schedaIndizi(taccuino)),
                scheda("Testimonianze", schedaTestimonianze(taccuino)),
                scheda("Sospettati", schedaSospettati(taccuino)),
                scheda("Appunti", schedaAppunti(taccuino)));

        Scene scene = new Scene(schede, 640, 500);
        URL css = RisorseGrafiche.foglioStile();
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        Stage stage = new Stage();
        stage.initOwner(proprietario);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Taccuino di " + partita.getInvestigatore().getNome());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private Tab scheda(String titolo, javafx.scene.Node contenuto) {
        Tab tab = new Tab(titolo, contenuto);
        tab.setClosable(false);
        return tab;
    }

    /** Scheda personaggio: progressione e spesa dei punti abilita sugli attributi. */
    private ScrollPane schedaInvestigatore(Partita partita) {
        VBox contenitore = contenitore();
        aggiornaSchedaInvestigatore(contenitore, partita);
        return scorrevole(contenitore);
    }

    /** (Ri)popola la scheda investigatore, cosi da riflettere la spesa dei punti. */
    private void aggiornaSchedaInvestigatore(VBox contenitore, Partita partita) {
        Investigatore inv = partita.getInvestigatore();
        contenitore.getChildren().clear();
        contenitore.getChildren().add(card("Investigatore " + inv.getNome(),
                "Livello " + inv.getLivello()
                        + "\nEsperienza: " + inv.getEsperienza() + " / " + inv.esperienzaProssimoLivello() + " PE"
                        + "\nPunti abilita disponibili: " + inv.getPuntiAbilita()
                        + "\nStile investigativo: " + inv.attributoDominante().etichetta()
                        + " (sblocca domande dedicate nei dialoghi)"));

        for (Attributo attributo : Attributo.values()) {
            Label nome = new Label(attributo.etichetta());
            nome.getStyleClass().add("taccuino-card-titolo");
            nome.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nome, Priority.ALWAYS);

            Label valore = new Label(String.valueOf(inv.getAttributo(attributo)));
            valore.getStyleClass().add("attributo-valore");

            Button potenzia = new Button("+");
            potenzia.getStyleClass().add("bottone-attributo");
            potenzia.setDisable(inv.getPuntiAbilita() <= 0);
            potenzia.setOnAction(e -> {
                inv.potenzia(attributo);
                aggiornaSchedaInvestigatore(contenitore, partita);
            });

            HBox riga = new HBox(10, nome, valore, potenzia);
            riga.setAlignment(Pos.CENTER_LEFT);
            riga.getStyleClass().add("taccuino-card");
            contenitore.getChildren().add(riga);
        }
    }

    private ScrollPane schedaIndizi(Taccuino taccuino) {
        VBox contenitore = contenitore();
        if (taccuino.getIndizi().isEmpty()) {
            contenitore.getChildren().add(vuoto("Nessun indizio raccolto. Esplora e ispeziona le stanze."));
        }
        for (Indizio indizio : taccuino.getIndizi()) {
            contenitore.getChildren().add(card(indizio.getNome() + "  [" + indizio.getTipo() + "]",
                    indizio.getDescrizione()));
        }
        return scorrevole(contenitore);
    }

    private ScrollPane schedaTestimonianze(Taccuino taccuino) {
        VBox contenitore = contenitore();
        Map<String, List<Testimonianza>> perFonte = taccuino.getTestimonianzePerFonte();
        if (perFonte.isEmpty()) {
            contenitore.getChildren().add(vuoto("Nessuna testimonianza raccolta. Parla con i presenti."));
        }
        perFonte.forEach((fonte, testimonianze) -> {
            Label intestazione = new Label(fonte);
            intestazione.getStyleClass().add("taccuino-fonte");
            contenitore.getChildren().add(intestazione);
            testimonianze.forEach(t -> contenitore.getChildren().add(card(null, "“" + t.testo() + "”")));
        });
        return scorrevole(contenitore);
    }

    private ScrollPane schedaSospettati(Taccuino taccuino) {
        VBox contenitore = contenitore();
        if (taccuino.getSospettatiNoti().isEmpty()) {
            contenitore.getChildren().add(vuoto("Nessun sospettato ancora interrogato."));
        }
        for (Sospettato s : taccuino.getSospettatiNoti()) {
            contenitore.getChildren().add(card(s.getNome() + "  (" + s.getRuolo() + ")",
                    "Movente: " + s.getMovente() + "\nAlibi dichiarato: " + s.getAlibiDichiarato()));
        }
        return scorrevole(contenitore);
    }

    private VBox schedaAppunti(Taccuino taccuino) {
        VBox contenitore = contenitore();
        VBox lista = new VBox(6);
        taccuino.getAppunti().forEach(a -> lista.getChildren().add(card(null, a)));

        TextArea editor = new TextArea();
        editor.setPromptText("Scrivi un appunto e premi Aggiungi...");
        editor.setPrefRowCount(3);
        editor.setWrapText(true);

        Button aggiungi = new Button("Aggiungi appunto");
        aggiungi.getStyleClass().add("bottone-principale");
        aggiungi.setOnAction(e -> {
            String testo = editor.getText().trim();
            if (!testo.isBlank()) {
                taccuino.aggiungiAppunto(testo);
                lista.getChildren().add(card(null, testo));
                editor.clear();
            }
        });

        contenitore.getChildren().addAll(lista, editor, aggiungi);
        return contenitore;
    }

    private VBox card(String titolo, String corpo) {
        VBox card = new VBox(4);
        card.getStyleClass().add("taccuino-card");
        if (titolo != null) {
            Label t = new Label(titolo);
            t.getStyleClass().add("taccuino-card-titolo");
            card.getChildren().add(t);
        }
        Label c = new Label(corpo);
        c.getStyleClass().add("taccuino-card-corpo");
        c.setWrapText(true);
        card.getChildren().add(c);
        return card;
    }

    private Label vuoto(String testo) {
        Label label = new Label(testo);
        label.getStyleClass().add("taccuino-vuoto");
        label.setWrapText(true);
        return label;
    }

    private VBox contenitore() {
        VBox box = new VBox(10);
        box.getStyleClass().add("taccuino-contenuto");
        box.setPadding(new Insets(16));
        return box;
    }

    private ScrollPane scorrevole(VBox contenuto) {
        ScrollPane scroll = new ScrollPane(contenuto);
        scroll.setFitToWidth(true);
        return scroll;
    }
}
