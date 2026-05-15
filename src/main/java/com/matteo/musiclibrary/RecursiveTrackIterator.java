package com.matteo.musiclibrary;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iteratore custom che visita un albero di LibraryNode in profondita' (depth-first)
 * e restituisce solo le foglie (Track), saltando le Collection.
 *
 * Disaccoppia completamente la struttura interna del Composite dal modo in cui
 * il client itera sui suoi contenuti (pattern Iterator del GoF).
 */
public class RecursiveTrackIterator implements Iterator<Track> {

    private final Deque<LibraryNode> stack;

    /**
     * Costruisce un iteratore a partire dalla lista di figli di una Collection.
     * I figli vengono inseriti nella pila in ordine inverso per preservare
     * l'ordine originale durante l'iterazione (pila e' LIFO).
     */
    public RecursiveTrackIterator(List<LibraryNode> roots) {
        this.stack = new ArrayDeque<>();
        // Inserisco in ordine inverso cosi' il primo elemento esce per primo
        for (int i = roots.size() - 1; i >= 0; i--) {
            this.stack.push(roots.get(i));
        }
    }

    @Override
    public boolean hasNext() {
        // Cerco in cima alla pila finche' trovo una Track o finisco gli elementi.
        // Se in cima c'e' una Collection, la espando e ripeto.
        while (!this.stack.isEmpty() && !(this.stack.peek() instanceof Track)) {
            LibraryNode top = this.stack.pop();
            if (top instanceof Collection) {
                Collection c = (Collection) top;
                List<LibraryNode> children = c.getChildren();
                // Inserisco i figli in ordine inverso
                for (int i = children.size() - 1; i >= 0; i--) {
                    this.stack.push(children.get(i));
                }
            }
        }
        return !this.stack.isEmpty();
    }

    @Override
    public Track next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Nessuna altra Track disponibile");
        }
        return (Track) this.stack.pop();
    }
}
