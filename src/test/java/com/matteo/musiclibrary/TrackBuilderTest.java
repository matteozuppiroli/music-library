package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrackBuilderTest {

    @Test
    void builder_con_tutti_i_campi_costruisce_track_completa() {
        Track t = new Track.Builder("Brown Sugar", "D'Angelo")
            .bpm(93)
            .key("Eb")
            .durationSeconds(280)
            .genre("R&B")
            .build();

        assertEquals("Brown Sugar", t.getTitle());
        assertEquals("D'Angelo", t.getArtist());
        assertEquals(93, t.getBpm());
        assertEquals("Eb", t.getKey());
        assertEquals(280, t.getDurationSeconds());
        assertEquals("R&B", t.getGenre());
    }

    @Test
    void builder_con_solo_campi_obbligatori_usa_default_sensati() {
        Track t = new Track.Builder("Test", "Artist").build();

        assertEquals("Test", t.getTitle());
        assertEquals("Artist", t.getArtist());
        // I default devono produrre un oggetto valido
        assertEquals(120, t.getBpm());
        assertEquals(180, t.getDurationSeconds());
    }

    @Test
    void builder_ordine_dei_metodi_non_e_significativo() {
        // Costruisco lo stesso oggetto con due ordini diversi
        Track t1 = new Track.Builder("Title", "Artist")
            .bpm(90).key("Am").durationSeconds(200).genre("R&B")
            .build();

        Track t2 = new Track.Builder("Title", "Artist")
            .genre("R&B").durationSeconds(200).key("Am").bpm(90)
            .build();

        assertEquals(t1.getBpm(), t2.getBpm());
        assertEquals(t1.getKey(), t2.getKey());
        assertEquals(t1.getDurationSeconds(), t2.getDurationSeconds());
        assertEquals(t1.getGenre(), t2.getGenre());
    }

    @Test
    void builder_supporta_campi_opzionali_lasciati_null() {
        Track t = new Track.Builder("Title", "Artist")
            .bpm(95)
            .durationSeconds(220)
            .build();
        // key e genre non sono stati impostati, sono null

        assertEquals(null, t.getKey());
        assertEquals(null, t.getGenre());
        assertEquals(95, t.getBpm());
    }

    @Test
    void builder_propaga_validazione_di_Track_titolo_vuoto() {
        Track.Builder b = new Track.Builder("", "Artist");
        // Il fallimento avviene al build(), non prima
        assertThrows(IllegalArgumentException.class, () -> b.build());
    }

    @Test
    void builder_propaga_validazione_di_Track_bpm_invalido() {
        Track.Builder b = new Track.Builder("Title", "Artist").bpm(500);
        assertThrows(IllegalArgumentException.class, () -> b.build());
    }

    @Test
    void builder_propaga_validazione_di_Track_durata_negativa() {
        Track.Builder b = new Track.Builder("Title", "Artist").durationSeconds(-10);
        assertThrows(IllegalArgumentException.class, () -> b.build());
    }

    @Test
    void builder_chaining_ritorna_lo_stesso_oggetto() {
        Track.Builder b = new Track.Builder("Title", "Artist");

        // Verifica che ogni metodo ritorni lo stesso Builder (per il chaining)
        assertEquals(b, b.bpm(90));
        assertEquals(b, b.key("C"));
        assertEquals(b, b.durationSeconds(200));
        assertEquals(b, b.genre("R&B"));
    }
}