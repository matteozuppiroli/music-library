package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LibraryServiceTest {

    // ===== createTrack =====

    @Test
    void createTrack_con_dati_validi_ritorna_success() {
        LibraryService service = new LibraryService();

        Result<Track> r = service.createTrack(
            "Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B");

        assertTrue(r.isSuccess());
        assertEquals("Brown Sugar", r.getValue().getTitle());
    }

    @Test
    void createTrack_con_titolo_vuoto_ritorna_failure_senza_lanciare_eccezione() {
        LibraryService service = new LibraryService();

        Result<Track> r = service.createTrack(
            "", "D'Angelo", 93, "Eb", 280, "R&B");

        assertFalse(r.isSuccess());
        assertNotNull(r.getError());
        // Il messaggio NON deve contenere riferimenti a classi interne (no information disclosure)
        assertFalse(r.getError().contains("Exception"));
        assertFalse(r.getError().contains("at com.matteo"));
    }

    @Test
    void createTrack_con_bpm_invalido_ritorna_failure() {
        LibraryService service = new LibraryService();

        Result<Track> r = service.createTrack(
            "Title", "Artist", 30, "C", 200, "R&B");

        assertFalse(r.isSuccess());
        assertTrue(r.getError().toLowerCase().contains("bpm")
                   || r.getError().toLowerCase().contains("non validi"));
    }

    // ===== createCollection =====

    @Test
    void createCollection_con_nome_valido_ritorna_success() {
        LibraryService service = new LibraryService();

        Result<Collection> r = service.createCollection("Mix");

        assertTrue(r.isSuccess());
        assertEquals("Mix", r.getValue().getName());
    }

    @Test
    void createCollection_con_nome_vuoto_ritorna_failure() {
        LibraryService service = new LibraryService();

        Result<Collection> r = service.createCollection("");

        assertFalse(r.isSuccess());
    }

    // ===== addNode =====

    @Test
    void addNode_con_dati_validi_ritorna_success() {
        LibraryService service = new LibraryService();
        Collection c = service.createCollection("Mix").getValue();
        Track t = service.createTrack("Untitled", "D'Angelo", 78, "C", 432, "R&B").getValue();

        Result<Collection> r = service.addNode(c, t);

        assertTrue(r.isSuccess());
        assertEquals(1, r.getValue().getTrackCount());
    }

    @Test
    void addNode_con_duplicato_ritorna_failure_con_messaggio_pulito() {
        LibraryService service = new LibraryService();
        Collection c = service.createCollection("Mix").getValue();
        Track t = service.createTrack("Untitled", "D'Angelo", 78, "C", 432, "R&B").getValue();
        service.addNode(c, t);   // prima aggiunta

        Result<Collection> r = service.addNode(c, t);   // duplicato

        assertFalse(r.isSuccess());
        assertNotNull(r.getError());
        // Il messaggio e' pulito: niente stack trace, niente classi interne
        assertFalse(r.getError().contains("Exception"));
        assertFalse(r.getError().contains("at com.matteo"));
    }

    @Test
    void addNode_su_collezione_nulla_ritorna_failure() {
        LibraryService service = new LibraryService();
        Track t = service.createTrack("Untitled", "D'Angelo", 78, "C", 432, "R&B").getValue();

        Result<Collection> r = service.addNode(null, t);

        assertFalse(r.isSuccess());
    }
}