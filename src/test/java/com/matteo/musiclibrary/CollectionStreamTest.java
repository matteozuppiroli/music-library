package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionStreamTest {

    // ===== streamTracks =====

    @Test
    void streamTracks_su_collezione_vuota_produce_stream_vuoto() {
        Collection c = new Collection("Vuota");

        long count = c.streamTracks().count();

        assertEquals(0L, count);
    }

    @Test
    void streamTracks_visita_ricorsivamente_anche_le_annidate() {
        Collection esterna = new Collection("Tutta");
        Collection sotto = new Collection("R&B");

        esterna.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        sotto.add(new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B"));
        sotto.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));
        esterna.add(sotto);

        long count = esterna.streamTracks().count();

        assertEquals(3L, count);
    }

    // ===== findTracksByBpmRange =====

    @Test
    void findTracksByBpmRange_filtra_correttamente() {
        Collection c = new Collection("Mix");
        c.add(new Track("Slow", "Artist1", 70, "C", 200, "Soul"));
        c.add(new Track("Medium", "Artist2", 90, "Am", 250, "R&B"));
        c.add(new Track("Fast", "Artist3", 120, "G", 220, "Pop"));

        List<Track> result = c.findTracksByBpmRange(85, 100);

        assertEquals(1, result.size());
        assertEquals("Medium", result.get(0).getTitle());
    }

    @Test
    void findTracksByBpmRange_con_range_invalido_lancia_eccezione() {
        Collection c = new Collection("Mix");
        assertThrows(IllegalArgumentException.class,
                     () -> c.findTracksByBpmRange(100, 50));
    }

    @Test
    void findTracksByBpmRange_estremi_inclusivi() {
        Collection c = new Collection("Mix");
        c.add(new Track("A", "X", 90, "C", 200, "R&B"));
        c.add(new Track("B", "Y", 100, "C", 200, "R&B"));

        List<Track> result = c.findTracksByBpmRange(90, 100);

        assertEquals(2, result.size());
    }

    // ===== findTracksByKey =====

    @Test
    void findTracksByKey_filtra_case_insensitive() {
        Collection c = new Collection("Mix");
        c.add(new Track("A", "X", 90, "Am", 200, "R&B"));
        c.add(new Track("B", "Y", 95, "AM", 200, "R&B"));
        c.add(new Track("C", "Z", 100, "C", 200, "R&B"));

        List<Track> result = c.findTracksByKey("am");

        assertEquals(2, result.size());
    }

    @Test
    void findTracksByKey_salta_tracce_con_key_null() {
        Collection c = new Collection("Mix");
        c.add(new Track("A", "X", 90, null, 200, "R&B"));
        c.add(new Track("B", "Y", 95, "Am", 200, "R&B"));

        List<Track> result = c.findTracksByKey("Am");

        assertEquals(1, result.size());
        assertEquals("B", result.get(0).getTitle());
    }

    // ===== findTracksByGenre =====

    @Test
    void findTracksByGenre_filtra_correttamente() {
        Collection c = new Collection("Mix");
        c.add(new Track("A", "X", 90, "Am", 200, "R&B"));
        c.add(new Track("B", "Y", 95, "C", 200, "Soul"));
        c.add(new Track("C", "Z", 100, "G", 200, "R&B"));

        List<Track> result = c.findTracksByGenre("r&b");

        assertEquals(2, result.size());
    }

    // ===== getAverageBpm =====

    @Test
    void getAverageBpm_calcola_media() {
        Collection c = new Collection("Mix");
        c.add(new Track("A", "X", 80, "C", 200, "R&B"));
        c.add(new Track("B", "Y", 100, "C", 200, "R&B"));
        c.add(new Track("C", "Z", 120, "C", 200, "R&B"));

        OptionalDouble avg = c.getAverageBpm();

        assertTrue(avg.isPresent());
        assertEquals(100.0, avg.getAsDouble(), 0.01);
    }

    @Test
    void getAverageBpm_su_collezione_vuota_e_vuoto() {
        Collection c = new Collection("Vuota");

        OptionalDouble avg = c.getAverageBpm();

        assertFalse(avg.isPresent());
    }

    // ===== getTracksGroupedByGenre =====

    @Test
    void getTracksGroupedByGenre_raggruppa_correttamente() {
        Collection c = new Collection("Mix");
        c.add(new Track("A", "X", 90, "C", 200, "R&B"));
        c.add(new Track("B", "Y", 95, "C", 200, "Soul"));
        c.add(new Track("C", "Z", 100, "C", 200, "R&B"));

        Map<String, List<Track>> grouped = c.getTracksGroupedByGenre();

        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get("R&B").size());
        assertEquals(1, grouped.get("Soul").size());
    }

    @Test
    void getTracksGroupedByGenre_tracce_senza_genere_sotto_Sconosciuto() {
        Collection c = new Collection("Mix");
        c.add(new Track("A", "X", 90, "C", 200, null));   // genere null
        c.add(new Track("B", "Y", 95, "C", 200, "R&B"));

        Map<String, List<Track>> grouped = c.getTracksGroupedByGenre();

        assertEquals(2, grouped.size());
        assertEquals(1, grouped.get("Sconosciuto").size());
        assertEquals(1, grouped.get("R&B").size());
    }
}