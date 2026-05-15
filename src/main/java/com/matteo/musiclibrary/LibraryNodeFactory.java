package com.matteo.musiclibrary;

import java.util.Map;

/**
 * Factory per la creazione di LibraryNode (Track e Collection) a partire
 * da specifiche esterne (tipo + parametri).
 *
 * Centralizza la logica di costruzione, isolando il resto del codice dalle
 * classi concrete e fornendo un singolo punto di controllo per la validazione
 * dei parametri di ingresso. Pattern Factory del GoF.
 *
 * Classe utility: tutti i metodi sono statici, l'istanziazione non e' supportata.
 */
public class LibraryNodeFactory {

    /**
     * Costruttore privato: impedisce l'istanziazione di questa classe utility.
     */
    private LibraryNodeFactory() {
        throw new UnsupportedOperationException("Classe utility, non istanziabile");
    }

    /**
     * Crea una Track delegando al costruttore della classe.
     * Punto di ingresso centralizzato: in futuro permette di aggiungere
     * logging, tracing o validazioni globali senza modificare i client.
     */
    public static Track createTrack(String title, String artist, int bpm,
                                    String key, int durationSeconds, String genre) {
        return new Track(title, artist, bpm, key, durationSeconds, genre);
    }

    /**
     * Crea una Collection vuota delegando al costruttore della classe.
     */
    public static Collection createCollection(String name) {
        return new Collection(name);
    }

    /**
     * Crea un LibraryNode polimorficamente a partire da un tipo testuale
     * e da una mappa di parametri.
     *
     * Tipi supportati:
     *  - "track":      richiede title, artist, bpm, durationSeconds.
     *                  Opzionali: key, genre.
     *  - "collection": richiede name. Crea una collezione vuota.
     *
     * @param type   il tipo di nodo da creare ("track" o "collection")
     * @param params mappa dei parametri
     * @return il LibraryNode costruito
     * @throws IllegalArgumentException se il tipo e' sconosciuto o se mancano parametri obbligatori
     */
    public static LibraryNode createFromType(String type, Map<String, String> params) {
        if (type == null) {
            throw new IllegalArgumentException("Il tipo non puo' essere nullo");
        }
        if (params == null) {
            throw new IllegalArgumentException("I parametri non possono essere nulli");
        }

        String normalizedType = type.trim().toLowerCase();

        switch (normalizedType) {
            case "track":
                return createTrackFromParams(params);
            case "collection":
                return createCollectionFromParams(params);
            default:
                throw new IllegalArgumentException("Tipo di nodo sconosciuto: " + type);
        }
    }

    // ===== Metodi privati di supporto =====

    private static Track createTrackFromParams(Map<String, String> params) {
        String title = params.get("title");
        String artist = params.get("artist");
        String bpmStr = params.get("bpm");
        String key = params.get("key");
        String durationStr = params.get("durationSeconds");
        String genre = params.get("genre");

        if (bpmStr == null) {
            throw new IllegalArgumentException("Parametro obbligatorio mancante: bpm");
        }
        if (durationStr == null) {
            throw new IllegalArgumentException("Parametro obbligatorio mancante: durationSeconds");
        }

        int bpm;
        int duration;
        try {
            bpm = Integer.parseInt(bpmStr);
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bpm e durationSeconds devono essere numeri interi");
        }

        return new Track(title, artist, bpm, key, duration, genre);
    }

    private static Collection createCollectionFromParams(Map<String, String> params) {
        String name = params.get("name");
        return new Collection(name);
    }
}