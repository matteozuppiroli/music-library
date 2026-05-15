package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LibraryRepositoryTest {

    @Test
    void salva_e_ricarica_una_collection_semplice(@TempDir Path tempDir) {
        LibraryRepository repo = new LibraryRepository();
        Collection original = new Collection("Mix");
        original.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        original.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));

        Path file = tempDir.resolve("library.json");

        Result<String> saveResult = repo.save(original, file.toString());
        assertTrue(saveResult.isSuccess());

        Result<LibraryNode> loadResult = repo.load(file.toString());
        assertTrue(loadResult.isSuccess());

        LibraryNode loaded = loadResult.getValue();
        assertEquals("Mix", loaded.getName());
        assertEquals(2, loaded.getTrackCount());
        assertEquals(280 + 432, loaded.getTotalDurationSeconds());
    }

    @Test
    void salva_e_ricarica_una_collection_annidata(@TempDir Path tempDir) {
        LibraryRepository repo = new LibraryRepository();
        Collection esterna = new Collection("Tutta la musica");
        Collection sotto = new Collection("R&B 90s");
        sotto.add(new Track("One in a Million", "Aaliyah", 90, "Am", 270, "R&B"));
        sotto.add(new Track("Brown Sugar", "D'Angelo", 93, "Eb", 280, "R&B"));
        esterna.add(sotto);
        esterna.add(new Track("Untitled", "D'Angelo", 78, "C", 432, "R&B"));

        Path file = tempDir.resolve("nested.json");

        repo.save(esterna, file.toString());
        Result<LibraryNode> loadResult = repo.load(file.toString());

        assertTrue(loadResult.isSuccess());
        assertEquals(3, loadResult.getValue().getTrackCount());
        assertEquals(270 + 280 + 432, loadResult.getValue().getTotalDurationSeconds());
    }

    @Test
    void salva_e_ricarica_una_collection_vuota(@TempDir Path tempDir) {
        LibraryRepository repo = new LibraryRepository();
        Collection vuota = new Collection("Vuota");

        Path file = tempDir.resolve("empty.json");

        repo.save(vuota, file.toString());
        Result<LibraryNode> loadResult = repo.load(file.toString());

        assertTrue(loadResult.isSuccess());
        assertEquals(0, loadResult.getValue().getTrackCount());
    }

    @Test
    void load_di_file_inesistente_ritorna_failure() {
        LibraryRepository repo = new LibraryRepository();

        Result<LibraryNode> r = repo.load("/tmp/file-che-non-esiste-12345.json");

        assertFalse(r.isSuccess());
        assertTrue(r.getError().contains("non trovato"));
    }

    @Test
    void load_di_file_con_json_invalido_ritorna_failure(@TempDir Path tempDir) throws Exception {
        LibraryRepository repo = new LibraryRepository();
        Path file = tempDir.resolve("malformed.json");
        java.nio.file.Files.writeString(file, "questo non e' JSON");

        Result<LibraryNode> r = repo.load(file.toString());

        assertFalse(r.isSuccess());
    }

    @Test
    void save_con_nodo_nullo_ritorna_failure(@TempDir Path tempDir) {
        LibraryRepository repo = new LibraryRepository();
        Path file = tempDir.resolve("x.json");

        Result<String> r = repo.save(null, file.toString());

        assertFalse(r.isSuccess());
    }

    @Test
    void save_con_path_vuoto_ritorna_failure() {
        LibraryRepository repo = new LibraryRepository();
        Collection c = new Collection("Mix");

        Result<String> r = repo.save(c, "");

        assertFalse(r.isSuccess());
    }
}