package com.matteo.musiclibrary;

/**
 * Contratto comune del pattern Composite per la libreria musicale.
 * Implementato sia da Track (foglia) che da Collection (composito).
 * 
 * Permette di trattare uniformemente tracce singole e collezioni annidate,
 * abilitando operazioni ricorsive come il conteggio delle tracce e
 * il calcolo della durata totale.
 */

public interface LibraryNode {

    /**
     * @return il nome del nodo (titolo per Track, nome per Collection)
     */
    String getName();

    /**
     * @return il numero di tracce contenute nel nodo.
     *          Per Track è sempre 1; per Collection è la somma ricorsiva sui figli.
     */
    int getTrackCount();

    /**
     * @return la durata totale in secondi del nodo.
     *          Per Track è la propria durata; per Collection è la somma ricorsiva sui figli.
     */
    int getTotalDurationSeconds();
}
