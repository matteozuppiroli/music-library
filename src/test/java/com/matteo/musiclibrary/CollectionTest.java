package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CollectionTest {

    // ===== Costruzione e validazione =====

    @Test
    void nome_nullo_lancia_eccezione() {
        assertThrows(IllegalArgumentException.class, () -> new Collection(null));
    }

    @Test
    void nome_vuoto_lancia_eccezione() {
        assertThrows(IllegalArgumentException.class, () -> new Collection("   "));
    }

    @Test
    void collezione_appena_creata_ha_il_nome_corretto() {
        Collection c = new Collection("R&B Slow");
        assertEquals("R&B Slow", c.getName());
    }

    // ===== Comportamento di una Collection vuota =====

    @Test
    void collezione_vuota_ha_zero_tracce() {
        Collection c = new Collection("Vuota");
        assertEquals(0, c.getTrackCount());
    }

    @Test
    void collezione_vuota_ha_durata_zero() {
        Collection c = new Collection("Vuota");
        assertEquals(0, c.getTotalDurationSeconds());
    }

    // ===== Aggiunta di Track =====

    @Test
    void aggiungere_una_traccia_aumenta_il_conteggio() {
        Collection c = new Collection("Mix");
        Track t = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");

        c.add(t);

        assertEquals(1, c.getTrackCount());
        assertEquals(432, c.getTotalDurationSeconds());
    }

    @Test
    void aggiungere_nodo_nullo_lancia_eccezione() {
        Collection c = new Collection("Mix");
        assertThrows(IllegalArgumentException.class, () -> c.add(null));
    }

    @Test
    void aggiungere_la_stessa_traccia_due_volte_lancia_eccezione() {
        Collection c = new Collection("Mix");
        Track t = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");

        c.add(t);

        assertThrows(DuplicateNodeException.class, () -> c.add(t));
    }

    @Test
    void aggiungere_una_traccia_semanticamente_uguale_lancia_eccezione() {
        Collection c = new Collection("Mix");
        Track a = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");
        Track b = new Track("untitled", "d'angelo", 80, "C#", 430, "Soul"); // stesso brano+artista

        c.add(a);

        assertThrows(DuplicateNodeException.class, () -> c.add(b));
    }

    // ===== Annidamento ricorsivo (cuore del Composite) =====

    @Test
    void collezione_annidata_somma_correttamente_il_conteggio_dei_figli() {
        // Una collezione esterna che contiene una traccia + una sotto-collezione con 2 tracce
        Collection esterna = new Collection("Tutta la musica");
        Collection sotto = new Collection("R&B 90s");

        esterna.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        sotto.add(new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B"));
        sotto.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));
        esterna.add(sotto);

        // Conteggio totale: 1 (Brown Sugar) + 2 (dentro sotto) = 3
        assertEquals(3, esterna.getTrackCount());
    }

    @Test
    void collezione_annidata_somma_correttamente_la_durata_totale() {
        Collection esterna = new Collection("Tutta la musica");
        Collection sotto = new Collection("R&B 90s");

        esterna.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        sotto.add(new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B"));
        sotto.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));
        esterna.add(sotto);

        // Durata totale: 280 + 270 + 432 = 982
        assertEquals(982, esterna.getTotalDurationSeconds());
    }

    @Test
    void annidamento_a_tre_livelli_funziona_correttamente() {
        // Livello 1: Tutto > Livello 2: 90s > Livello 3: R&B
        Collection tutto = new Collection("Tutto");
        Collection anni90 = new Collection("90s");
        Collection rnb = new Collection("R&B");

        rnb.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        rnb.add(new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B"));

        anni90.add(rnb);
        tutto.add(anni90);

        // Anche a 3 livelli di profondita', il conteggio funziona
        assertEquals(2, tutto.getTrackCount());
        assertEquals(280 + 270, tutto.getTotalDurationSeconds());
    }

    // ===== Encapsulation: la lista dei figli e' immutabile dall'esterno =====

    @Test
    void getChildren_ritorna_una_lista_non_modificabile() {
        Collection c = new Collection("Mix");
        c.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));

        // Provare a modificare la lista esposta deve fallire
        assertThrows(UnsupportedOperationException.class,
                     () -> c.getChildren().clear());
    }

    @Test
    void getChildren_riflette_le_tracce_effettivamente_aggiunte() {
        Collection c = new Collection("Mix");
        Track t = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");

        c.add(t);

        assertEquals(1, c.getChildren().size());
        assertTrue(c.getChildren().contains(t));
    }

    // ===== Pattern Iterator =====

    @Test
    void iteratore_su_collezione_vuota_non_ha_elementi() {
        Collection c = new Collection("Vuota");

        Iterator<Track> it = c.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    void iteratore_su_collezione_piatta_restituisce_tutte_le_tracce_in_ordine() {
        Collection c = new Collection("Mix");
        Track t1 = new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B");
        Track t2 = new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B");
        Track t3 = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");

        c.add(t1);
        c.add(t2);
        c.add(t3);

        Iterator<Track> it = c.iterator();
        assertEquals(t1, it.next());
        assertEquals(t2, it.next());
        assertEquals(t3, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void iteratore_visita_ricorsivamente_anche_le_sotto_collezioni() {
        Collection esterna = new Collection("Tutta");
        Collection sotto = new Collection("R&B 90s");

        Track t1 = new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B");
        Track t2 = new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B");
        Track t3 = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");

        esterna.add(t1);
        sotto.add(t2);
        sotto.add(t3);
        esterna.add(sotto);

        // L'iteratore deve produrre 3 tracce in totale (anche le annidate)
        long conteggio = esterna.streamTracks().count();
        assertEquals(3L, conteggio);
    }

    @Test
    void iteratore_supporta_for_each_e_visita_in_profondita() {
        Collection radice = new Collection("Tutto");
        Collection anni90 = new Collection("90s");
        Collection rnb = new Collection("R&B");

        rnb.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        rnb.add(new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B"));
        anni90.add(rnb);
        radice.add(anni90);
        radice.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));

        // 3 tracce totali a profondita' diverse
        long conteggio = radice.streamTracks().count();
        assertEquals(3L, conteggio);
    }

    @Test
    void chiamare_next_dopo_la_fine_lancia_NoSuchElementException() {
        Collection c = new Collection("Mix");
        c.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));

        Iterator<Track> it = c.iterator();
        it.next();   // consumo l'unica traccia

        assertThrows(NoSuchElementException.class, () -> it.next());
    }
}