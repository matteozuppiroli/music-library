package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LibraryHistoryTest {

    // ===== Save snapshot =====

    @Test
    void dopo_un_save_canUndo_e_true() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        history.saveSnapshot(c);

        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    // ===== Undo singolo =====

    @Test
    void undo_ripristina_lo_stato_precedente() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        // Stato iniziale: 0 tracce
        history.saveSnapshot(c);

        // Faccio una modifica
        c.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        assertEquals(1, c.getTrackCount());

        // Undo: torno allo stato iniziale (0 tracce)
        boolean undone = history.undo(c);

        assertTrue(undone);
        assertEquals(0, c.getTrackCount());
    }

    @Test
    void undo_senza_snapshot_ritorna_false() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        assertFalse(history.undo(c));
    }

    // ===== Redo =====

    @Test
    void redo_riesegue_operazione_annullata() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        history.saveSnapshot(c);
        c.add(new Track("Track1", "Artist1", 90, "C", 200, "R&B"));

        history.undo(c);
        assertEquals(0, c.getTrackCount());

        boolean redone = history.redo(c);

        assertTrue(redone);
        assertEquals(1, c.getTrackCount());
    }

    @Test
    void redo_senza_undo_precedente_ritorna_false() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        history.saveSnapshot(c);
        assertFalse(history.redo(c));
    }

    // ===== Sequenza undo + nuova modifica annulla redoStack =====

    @Test
    void nuova_operazione_dopo_undo_cancella_la_redo_stack() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        history.saveSnapshot(c);
        c.add(new Track("A", "X", 90, "C", 200, "R&B"));

        history.undo(c);   // torna a 0 tracce
        assertTrue(history.canRedo());

        // Nuova operazione: la redoStack deve essere cancellata
        history.saveSnapshot(c);
        c.add(new Track("B", "Y", 95, "C", 200, "R&B"));

        assertFalse(history.canRedo());
    }

    // ===== Undo/redo a piu' livelli =====

    @Test
    void undo_e_redo_funzionano_su_piu_operazioni() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        history.saveSnapshot(c);
        c.add(new Track("A", "X", 90, "C", 200, "R&B"));

        history.saveSnapshot(c);
        c.add(new Track("B", "Y", 95, "C", 200, "R&B"));

        history.saveSnapshot(c);
        c.add(new Track("C", "Z", 100, "C", 200, "R&B"));

        assertEquals(3, c.getTrackCount());

        // Undo 3 volte: torno a 0
        history.undo(c);
        assertEquals(2, c.getTrackCount());
        history.undo(c);
        assertEquals(1, c.getTrackCount());
        history.undo(c);
        assertEquals(0, c.getTrackCount());

        // Redo 3 volte: torno a 3
        history.redo(c);
        assertEquals(1, c.getTrackCount());
        history.redo(c);
        assertEquals(2, c.getTrackCount());
        history.redo(c);
        assertEquals(3, c.getTrackCount());
    }

    // ===== Deep copy =====

    @Test
    void modifiche_dopo_lo_snapshot_non_influenzano_il_memento() {
        Collection c = new Collection("Mix");
        c.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));

        LibraryHistory history = new LibraryHistory();
        history.saveSnapshot(c);

        // Modifico dopo lo snapshot
        c.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));
        assertEquals(2, c.getTrackCount());

        // L'undo deve riportarmi a 1 traccia (lo stato salvato), non 0 o 2
        history.undo(c);
        assertEquals(1, c.getTrackCount());
    }

    @Test
    void deep_copy_isola_le_sottocollezioni() {
        Collection esterna = new Collection("Esterna");
        Collection sotto = new Collection("Sotto");
        sotto.add(new Track("Track1", "Artist1", 90, "C", 200, "R&B"));
        esterna.add(sotto);

        LibraryHistory history = new LibraryHistory();
        history.saveSnapshot(esterna);

        // Modifico la sotto-collezione dopo lo snapshot
        sotto.add(new Track("Track2", "Artist2", 95, "C", 200, "R&B"));
        assertEquals(2, esterna.getTrackCount());

        // L'undo deve ripristinare lo stato con 1 traccia totale,
        // dimostrando che il memento ha fatto deep copy della sotto-collection
        history.undo(esterna);
        assertEquals(1, esterna.getTrackCount());
    }

    // ===== Validazione =====

    @Test
    void saveSnapshot_con_collection_null_lancia_eccezione() {
        LibraryHistory history = new LibraryHistory();
        assertThrows(IllegalArgumentException.class,
                     () -> history.saveSnapshot(null));
    }

    @Test
    void undo_con_collection_null_lancia_eccezione() {
        LibraryHistory history = new LibraryHistory();
        assertThrows(IllegalArgumentException.class,
                     () -> history.undo(null));
    }

    @Test
    void clear_svuota_le_pile() {
        Collection c = new Collection("Mix");
        LibraryHistory history = new LibraryHistory();

        history.saveSnapshot(c);
        history.clear();

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }
}