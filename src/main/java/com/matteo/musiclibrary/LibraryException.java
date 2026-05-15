package com.matteo.musiclibrary;

/**
 * Eccezione base per tutti gli errori di dominio della libreria musicale.
 *
 * Tutte le eccezioni specifiche del dominio estendono questa classe, permettendo
 * di catturare in modo uniforme qualunque errore "di business" al boundary del
 * sistema senza confondersi con eccezioni tecniche di Java.
 *
 * Unchecked (estende RuntimeException) per non inquinare le firme dei metodi
 * con dichiarazioni "throws" obbligatorie: la gestione e' centralizzata al
 * boundary, non sparpagliata ovunque.
 */
public class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}