package com.matteo.musiclibrary;

import java.util.List;

/**
 * Contratto del pattern Strategy per ordinare una lista di Track
 * secondo criteri intercambiabili a runtime.
 *
 * Ogni implementazione concreta definisce un criterio di ordinamento
 * specifico (BPM, durata, titolo, artista, ...).
 *
 * Nota didattica: Java offre gia' l'interfaccia Comparator&lt;T&gt; che
 * copre lo stesso caso d'uso. Qui ho preferito un'interfaccia esplicita
 * per rendere il pattern visibile nel codice.
 */
public interface SortStrategy {

    /**
     * Ordina la lista fornita secondo il criterio dell'implementazione.
     * Non modifica la lista in input: ritorna una nuova lista ordinata.
     *
     * @param tracks lista di tracce da ordinare (non viene modificata)
     * @return nuova lista ordinata secondo il criterio
     */
    List<Track> sort(List<Track> tracks);

    /**
     * @return nome leggibile del criterio (utile per logging e UI)
     */
    String getName();
}