package com.matteo.musiclibrary;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Caretaker del pattern Memento: gestisce le pile di undo e redo
 * per una Collection.
 *
 * Tiene riferimenti a CollectionMemento ma non guarda mai dentro:
 * preserva l'encapsulation dell'Originator (Collection).
 *
 * Comportamento:
 *  - saveSnapshot(collection): salva lo stato attuale, cancella la pila di redo
 *  - undo(collection): ripristina lo stato precedente
 *  - redo(collection): riesegue uno stato che era stato annullato
 */
public class LibraryHistory {

    private final Deque<Collection.CollectionMemento> undoStack = new ArrayDeque<>();
    private final Deque<Collection.CollectionMemento> redoStack = new ArrayDeque<>();

    /**
     * Salva uno snapshot della Collection corrente. Da chiamare PRIMA
     * di un'operazione che si vuole poter annullare.
     * Cancella la pila di redo (le operazioni "future" diventano irrilevanti
     * dopo una nuova operazione).
     */
    public void saveSnapshot(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("La collection non puo' essere null");
        }
        undoStack.push(collection.saveMemento());
        redoStack.clear();
    }

    /**
     * Annulla l'ultima operazione: ripristina la Collection allo stato
     * precedente al piu' recente saveSnapshot.
     *
     * @return true se l'undo e' stato effettuato, false se non c'era nulla da annullare
     */
    public boolean undo(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("La collection non puo' essere null");
        }
        if (undoStack.isEmpty()) {
            return false;
        }
        // Salvo lo stato attuale nella redo stack prima di ripristinare
        redoStack.push(collection.saveMemento());
        Collection.CollectionMemento previous = undoStack.pop();
        collection.restoreFromMemento(previous);
        return true;
    }

    /**
     * Riesegue un'operazione precedentemente annullata.
     *
     * @return true se il redo e' stato effettuato, false se non c'era nulla da rifare
     */
    public boolean redo(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("La collection non puo' essere null");
        }
        if (redoStack.isEmpty()) {
            return false;
        }
        undoStack.push(collection.saveMemento());
        Collection.CollectionMemento next = redoStack.pop();
        collection.restoreFromMemento(next);
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Cancella la cronologia (utile dopo "salva su file" per liberare memoria).
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}