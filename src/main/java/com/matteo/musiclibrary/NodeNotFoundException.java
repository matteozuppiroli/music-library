package com.matteo.musiclibrary;

/**
 * Lanciata quando si tenta di accedere a un nodo (Track o Collection)
 * che non esiste nella libreria.
 */
public class NodeNotFoundException extends LibraryException {

    public NodeNotFoundException(String name) {
        super("Nodo non trovato: " + name);
    }
}