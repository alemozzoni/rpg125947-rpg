package it.unicam.cs.mpgc.rpg125947.model.dialogo;

import it.unicam.cs.mpgc.rpg125947.model.Attributo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica i predicati di {@link OpzioneDialogo}: prova di abilita, stile
 * investigativo e disponibilita per un dato profilo.
 */
class OpzioneDialogoTest {

    private Testimonianza risposta() {
        return new Testimonianza("Tizio", "Risposta.", null);
    }

    @Test
    void unaDomandaUniversaleEDisponibilePerOgniStile() {
        OpzioneDialogo universale = new OpzioneDialogo("Domanda?", risposta());
        assertTrue(universale.stileRichiesto().isEmpty());
        for (Attributo a : Attributo.values()) {
            assertTrue(universale.disponibilePer(a));
        }
    }

    @Test
    void unaDomandaDiStileEDisponibileSoloPerIlSuoProfilo() {
        OpzioneDialogo dedicata = new OpzioneDialogo("Domanda?", risposta(), null, 0, Attributo.LOGICA);
        assertTrue(dedicata.stileRichiesto().isPresent());
        assertTrue(dedicata.disponibilePer(Attributo.LOGICA));
        assertFalse(dedicata.disponibilePer(Attributo.INTUITO));
    }

    @Test
    void ilVincoloDiStileEIndipendenteDallaProvaDiAbilita() {
        // Una domanda puo essere insieme prova di Eloquenza e riservata allo stile Logica.
        OpzioneDialogo opzione = new OpzioneDialogo("Domanda?", risposta(), Attributo.ELOQUENZA, 11, Attributo.LOGICA);
        assertTrue(opzione.richiedeProva());
        assertTrue(opzione.attributoRichiesto().orElseThrow() == Attributo.ELOQUENZA);
        assertTrue(opzione.disponibilePer(Attributo.LOGICA));
        assertFalse(opzione.disponibilePer(Attributo.ELOQUENZA));
    }
}
