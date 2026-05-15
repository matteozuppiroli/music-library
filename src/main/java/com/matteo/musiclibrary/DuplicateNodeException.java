package com.matteo.musiclibrary;

/**
 * Lanciata quando si tenta di aggiungere un nodo gia' presente in una collezione.
 */
public class DuplicateNodeException extends LibraryException {

    public DuplicateNodeException(String nodeName) {
        super("Elemento gia' presente nella collezione: " + nodeName);
    }
}