package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LibraryNodeFactoryTest {

    // ===== Metodi diretti =====

    @Test
    void createTrack_costruisce_una_track_valida() {
        Track t = LibraryNodeFactory.createTrack(
            "Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B");

        assertEquals("Brown Sugar", t.getTitle());
        assertEquals("D'Angelo", t.getArtist());
        assertEquals(93, t.getBpm());
    }

    @Test
    void createCollection_costruisce_una_collection_vuota() {
        Collection c = LibraryNodeFactory.createCollection("R&B Slow");

        assertEquals("R&B Slow", c.getName());
        assertEquals(0, c.getTrackCount());
    }

    // ===== Metodo polimorfico createFromType =====

    @Test
    void createFromType_track_costruisce_una_track() {
        Map<String, String> params = new HashMap<>();
        params.put("title", "Untitled");
        params.put("artist", "D'Angelo");
        params.put("bpm", "78");
        params.put("durationSeconds", "432");
        params.put("key", "C");
        params.put("genre", "Soul");

        LibraryNode node = LibraryNodeFactory.createFromType("track", params);

        assertTrue(node instanceof Track);
        assertEquals("Untitled", node.getName());
        assertEquals(1, node.getTrackCount());
    }

    @Test
    void createFromType_collection_costruisce_una_collection() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Mix Estate");

        LibraryNode node = LibraryNodeFactory.createFromType("collection", params);

        assertTrue(node instanceof Collection);
        assertEquals("Mix Estate", node.getName());
    }

    @Test
    void createFromType_normalizza_maiuscole_e_spazi() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Mix");

        LibraryNode node = LibraryNodeFactory.createFromType("  COLLECTION  ", params);

        assertTrue(node instanceof Collection);
    }

    // ===== Validazione =====

    @Test
    void createFromType_tipo_sconosciuto_lancia_eccezione() {
        Map<String, String> params = new HashMap<>();
        assertThrows(IllegalArgumentException.class,
                     () -> LibraryNodeFactory.createFromType("playlist", params));
    }

    @Test
    void createFromType_tipo_nullo_lancia_eccezione() {
        Map<String, String> params = new HashMap<>();
        assertThrows(IllegalArgumentException.class,
                     () -> LibraryNodeFactory.createFromType(null, params));
    }

    @Test
    void createFromType_params_nulli_lanciano_eccezione() {
        assertThrows(IllegalArgumentException.class,
                     () -> LibraryNodeFactory.createFromType("track", null));
    }

    @Test
    void createFromType_track_senza_bpm_lancia_eccezione() {
        Map<String, String> params = new HashMap<>();
        params.put("title", "Brown Sugar");
        params.put("artist", "D'Angelo");
        params.put("durationSeconds", "280");

        assertThrows(IllegalArgumentException.class,
                     () -> LibraryNodeFactory.createFromType("track", params));
    }

    @Test
    void createFromType_track_con_bpm_non_numerico_lancia_eccezione() {
        Map<String, String> params = new HashMap<>();
        params.put("title", "Brown Sugar");
        params.put("artist", "D'Angelo");
        params.put("bpm", "veloce");
        params.put("durationSeconds", "280");

        assertThrows(IllegalArgumentException.class,
                     () -> LibraryNodeFactory.createFromType("track", params));
    }

    @Test
    void factory_non_e_istanziabile() {
        assertEquals("Mix", LibraryNodeFactory.createCollection("Mix").getName());
    }
}