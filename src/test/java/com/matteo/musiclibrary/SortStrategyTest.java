package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SortStrategyTest {

    // ===== SortByBpm =====

    @Test
    void sortByBpm_ordina_dal_piu_lento_al_piu_veloce() {
        SortStrategy strategy = new SortByBpm();
        List<Track> tracks = List.of(
            new Track("C", "Z", 120, "C", 200, "Pop"),
            new Track("A", "X", 70, "C", 200, "Soul"),
            new Track("B", "Y", 95, "C", 200, "R&B")
        );

        List<Track> sorted = strategy.sort(tracks);

        assertEquals(70, sorted.get(0).getBpm());
        assertEquals(95, sorted.get(1).getBpm());
        assertEquals(120, sorted.get(2).getBpm());
    }

    @Test
    void sortByBpm_non_modifica_la_lista_originale() {
        SortStrategy strategy = new SortByBpm();
        List<Track> tracks = new java.util.ArrayList<>();
        tracks.add(new Track("C", "Z", 120, "C", 200, "Pop"));
        tracks.add(new Track("A", "X", 70, "C", 200, "Soul"));

        strategy.sort(tracks);

        // La lista originale e' rimasta com'era
        assertEquals(120, tracks.get(0).getBpm());
        assertEquals(70, tracks.get(1).getBpm());
    }

    // ===== SortByDuration =====

    @Test
    void sortByDuration_ordina_dalla_piu_corta_alla_piu_lunga() {
        SortStrategy strategy = new SortByDuration();
        List<Track> tracks = List.of(
            new Track("A", "X", 90, "C", 432, "Soul"),
            new Track("B", "Y", 90, "C", 200, "R&B"),
            new Track("C", "Z", 90, "C", 280, "Pop")
        );

        List<Track> sorted = strategy.sort(tracks);

        assertEquals(200, sorted.get(0).getDurationSeconds());
        assertEquals(280, sorted.get(1).getDurationSeconds());
        assertEquals(432, sorted.get(2).getDurationSeconds());
    }

    // ===== SortByTitle =====

    @Test
    void sortByTitle_ordina_alfabeticamente_case_insensitive() {
        SortStrategy strategy = new SortByTitle();
        List<Track> tracks = List.of(
            new Track("Brown Sugar", "X", 90, "C", 200, "R&B"),
            new Track("aaliyah", "Y", 90, "C", 200, "R&B"),
            new Track("Cup of Tea", "Z", 90, "C", 200, "R&B")
        );

        List<Track> sorted = strategy.sort(tracks);

        assertEquals("aaliyah", sorted.get(0).getTitle());
        assertEquals("Brown Sugar", sorted.get(1).getTitle());
        assertEquals("Cup of Tea", sorted.get(2).getTitle());
    }

    // ===== SortByArtist =====

    @Test
    void sortByArtist_ordina_alfabeticamente() {
        SortStrategy strategy = new SortByArtist();
        List<Track> tracks = List.of(
            new Track("A", "Z-artist", 90, "C", 200, "R&B"),
            new Track("B", "A-artist", 90, "C", 200, "R&B"),
            new Track("C", "M-artist", 90, "C", 200, "R&B")
        );

        List<Track> sorted = strategy.sort(tracks);

        assertEquals("A-artist", sorted.get(0).getArtist());
        assertEquals("M-artist", sorted.get(1).getArtist());
        assertEquals("Z-artist", sorted.get(2).getArtist());
    }

    // ===== Integrazione con Collection =====

    @Test
    void collection_sortBy_funziona_anche_su_collezioni_annidate() {
        Collection esterna = new Collection("Tutta");
        Collection sotto = new Collection("Annidata");

        esterna.add(new Track("A", "X", 120, "C", 200, "Pop"));
        sotto.add(new Track("B", "Y", 70, "C", 200, "Soul"));
        sotto.add(new Track("C", "Z", 90, "C", 200, "R&B"));
        esterna.add(sotto);

        List<Track> sorted = esterna.sortBy(new SortByBpm());

        assertEquals(3, sorted.size());
        assertEquals(70, sorted.get(0).getBpm());
        assertEquals(90, sorted.get(1).getBpm());
        assertEquals(120, sorted.get(2).getBpm());
    }

    @Test
    void collection_sortBy_con_strategy_null_lancia_eccezione() {
        Collection c = new Collection("Mix");
        assertThrows(IllegalArgumentException.class, () -> c.sortBy(null));
    }

    @Test
    void sortByBpm_con_lista_null_lancia_eccezione() {
        SortStrategy strategy = new SortByBpm();
        assertThrows(IllegalArgumentException.class, () -> strategy.sort(null));
    }

    // ===== getName per documentazione =====

    @Test
    void ogni_strategia_ha_un_nome_descrittivo() {
        assertEquals("BPM crescente", new SortByBpm().getName());
        assertEquals("Durata crescente", new SortByDuration().getName());
        assertEquals("Titolo A-Z", new SortByTitle().getName());
        assertEquals("Artista A-Z", new SortByArtist().getName());
    }
}