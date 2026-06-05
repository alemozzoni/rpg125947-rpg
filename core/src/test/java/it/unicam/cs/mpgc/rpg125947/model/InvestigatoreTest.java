package it.unicam.cs.mpgc.rpg125947.model;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la scheda personaggio dell'investigatore: attributi, accumulo di
 * esperienza, passaggi di livello e spesa dei punti abilita.
 */
class InvestigatoreTest {

    private Investigatore conAttributi(int oss, int intu, int elo, int log) {
        Map<Attributo, Integer> a = new EnumMap<>(Attributo.class);
        a.put(Attributo.OSSERVAZIONE, oss);
        a.put(Attributo.INTUITO, intu);
        a.put(Attributo.ELOQUENZA, elo);
        a.put(Attributo.LOGICA, log);
        return new Investigatore("Test", a);
    }

    @Test
    void ilProfiloStandardAssegnaTuttiGliAttributiAlValoreStandard() {
        Investigatore inv = new Investigatore("Standard");
        for (Attributo a : Attributo.values()) {
            assertEquals(Investigatore.VALORE_STANDARD, inv.getAttributo(a));
        }
        assertEquals(1, inv.getLivello());
        assertEquals(0, inv.getPuntiAbilita());
    }

    @Test
    void unAttributoMancanteVieneRifiutato() {
        Map<Attributo, Integer> parziale = new EnumMap<>(Attributo.class);
        parziale.put(Attributo.OSSERVAZIONE, 4);
        assertThrows(IllegalArgumentException.class, () -> new Investigatore("Monco", parziale));
    }

    @Test
    void esperienzaSottoLaSogliaNonFaSalireDiLivello() {
        Investigatore inv = conAttributi(2, 2, 2, 2);
        assertFalse(inv.aggiungiEsperienza(20)); // soglia liv.1 = 30
        assertEquals(1, inv.getLivello());
        assertEquals(20, inv.getEsperienza());
    }

    @Test
    void raggiungereLaSogliaFaSalireDiLivelloEAssegnaPunti() {
        Investigatore inv = conAttributi(2, 2, 2, 2);
        assertTrue(inv.aggiungiEsperienza(30));
        assertEquals(2, inv.getLivello());
        assertEquals(Investigatore.PUNTI_PER_LIVELLO, inv.getPuntiAbilita());
        assertEquals(0, inv.getEsperienza());
    }

    @Test
    void spendereUnPuntoAumentaLAttributoScelto() {
        Investigatore inv = conAttributi(2, 2, 2, 2);
        inv.aggiungiEsperienza(30);
        inv.potenzia(Attributo.LOGICA);
        assertEquals(3, inv.getAttributo(Attributo.LOGICA));
        assertEquals(Investigatore.PUNTI_PER_LIVELLO - 1, inv.getPuntiAbilita());
    }

    @Test
    void spendereSenzaPuntiDisponibiliEVietato() {
        Investigatore inv = conAttributi(2, 2, 2, 2);
        assertThrows(IllegalStateException.class, () -> inv.potenzia(Attributo.INTUITO));
    }

    @Test
    void loStileInvestigativoELAttributoPiuAlto() {
        assertEquals(Attributo.ELOQUENZA, conAttributi(2, 3, 6, 4).attributoDominante());
        assertEquals(Attributo.LOGICA, conAttributi(1, 2, 3, 5).attributoDominante());
    }

    @Test
    void aParitaDiValoreVinceIlPrimoInOrdine() {
        // OSSERVAZIONE precede gli altri nell'enum: a parita vince lei.
        assertEquals(Attributo.OSSERVAZIONE, conAttributi(4, 4, 4, 4).attributoDominante());
        assertEquals(Attributo.INTUITO, conAttributi(2, 5, 5, 1).attributoDominante());
    }

    @Test
    void loStileRifletteLaSpesaDeiPunti() {
        Investigatore inv = conAttributi(3, 3, 3, 3);
        inv.aggiungiEsperienza(30);
        inv.potenzia(Attributo.INTUITO);
        assertEquals(Attributo.INTUITO, inv.attributoDominante());
    }
}
