package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrackTest {

    @Test
    void traccia_valida_espone_correttamente_tutti_i_getter() {
        // Arrange + Act
        Track t = new Track ("One in a Million", "Aaliyah", 90, "Am", 270, "R&B");

        //Assert
        assertEquals("One in a Million", t.getTitle());
        assertEquals("Aaliyah", t.getArtist());
        assertEquals(90, t.getBpm());
        assertEquals("Am", t.getKey());
        assertEquals(270, t.getDurationSeconds());
        assertEquals("R&B", t.getGenre());
    }

    @Test
    void titolo_nullo_lancia_eccezione(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Track(null, "Aaliyah", 90, "Am", 270, "R&B");
        });
    }

    @Test
    void titolo_vuoto_lancia_eccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("   ", "Aaliyah", 90, "Am", 270, "R&B");
        });
    }

    @Test
    void artista_nullo_lancia_eccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("Title", null, 90, "Am", 270, "R&B");
        });
    }

    @Test
    void bpm_troppo_basso_lancia_eccezione(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("Title", "Artist", 30, "Am", 270, "R&B");
        });
    }

    @Test
    void bpm_troppo_alto_lancia_eccezione(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("Title", "Artist", 400, "Am", 270, "R&B");
        });
    }

    @Test
    void durata_negativa_lancia_eccezione(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("Title", "Artist", 90, "Am", -5, "R&B");
        });
    }

    // ===== Test del comportamento come LibraryNode (Composite) =====

    @Test
    void getName_di_una_traccia_ritorna_il_titolo() {
        Track t = new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B");

        assertEquals("One in a Million", t.getName());
    }

    @Test
    void getTrackCount_di_una_traccia_ritorna_sempre_uno() {
        Track t = new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B");

        assertEquals(1, t.getTrackCount());
    }

    @Test
    void getTotalDurationSeconds_di_una_traccia_ritorna_la_propria_durata() {
        Track t = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");

        assertEquals(432, t.getTotalDurationSeconds());
    }

    // ===== Test di equals/hashCode =====

    @Test
    void due_tracce_con_stesso_titolo_e_artista_sono_uguali() {
        Track a = new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B");
        Track b = new Track("Untitled", "D'Angelo", 80, "C#", 430, "Soul");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void confronto_uguaglianza_ignora_maiuscole_minuscole() {
        Track a = new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B");
        Track b = new Track("ONE IN A MILLION", "aaliyah", 90, "Am", 270, "R&B");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void due_tracce_con_artista_diverso_NON_sono_uguali() {
        Track a = new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B");
        Track b = new Track("Brown Sugar", "Beck", 93, "Eb", 280, "R&B");

        assertNotEquals(a, b);
    }
}
