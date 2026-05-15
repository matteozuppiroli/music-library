package com.matteo.musiclibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Composito del pattern Composite per la libreria musicale.
 *
 * Una Collection ha un nome ed un elenco ordinato di LibraryNode figli,
 * che possono essere Track (foglie) oppure altre Collection (annidamento ricorsivo).
 *
 * I metodi getTrackCount() e getTotalDurationSeconds() sommano ricorsivamente
 * sui figli, sfruttando il polimorfismo dell'interfaccia LibraryNode.
 *
 * Vincoli:
 *  - una Collection può essere vuota
 *  - non è permesso aggiungere lo stesso elemento due volte
 *    (uguaglianza definita dall'equals dell'elemento)
 */
public class Collection implements LibraryNode, Iterable<Track> {

    private final String name;
    private final List<LibraryNode> children;

    /**
     * Costruisce una Collection vuota con il nome specificato.
     *
     * @param name nome della collezione (obbligatorio, non vuoto)
     * @throws IllegalArgumentException se name e' null o vuoto
     */
    public Collection(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome della collezione non puo' essere vuoto");
        }
        this.name = name;
        this.children = new ArrayList<>();
    }

    /**
     * Aggiunge un LibraryNode (Track o Collection) come figlio di questa collezione.
     *
     * @param node il nodo da aggiungere (obbligatorio)
     * @throws IllegalArgumentException se node e' null o gia' presente
     */
    public void add(LibraryNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un nodo nullo");
        }
        if (this.children.contains(node)) {
            throw new DuplicateNodeException(node.getName());
        }
        this.children.add(node);
    }

    /**
     * @return vista non modificabile della lista dei figli
     */
    public List<LibraryNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    // ===== Implementazione di LibraryNode =====

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getTrackCount() {
        int total = 0;
        for (LibraryNode child : this.children) {
            total = total + child.getTrackCount();
        }
        return total;
    }

    @Override
    public int getTotalDurationSeconds() {
        int total = 0;
        for (LibraryNode child : this.children) {
            total = total + child.getTotalDurationSeconds();
        }
        return total;
    }

    // ===== Implementazione di Iterable<Track> (pattern Iterator) =====

    @Override
    public Iterator<Track> iterator() {
        return new RecursiveTrackIterator(this.children);
    }

    // ===== Stream API & Lambda (bonus) =====

    /**
     * Produce uno Stream di tutte le Track contenute nella collezione,
     * visitando ricorsivamente anche le sotto-collezioni.
     *
     * Riutilizza l'iteratore esistente come sorgente, garantendo coerenza
     * tra l'iterazione tradizionale (for-each) e quella funzionale (stream).
     *
     * @return uno Stream sequenziale di tutte le Track annidate
     */
    public Stream<Track> streamTracks() {
        Iterable<Track> iterable = this;   // 'this' e' Iterable<Track>
        return java.util.stream.StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Cerca le tracce con BPM compreso nell'intervallo [min, max] inclusi.
     */
    public List<Track> findTracksByBpmRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min deve essere <= max");
        }
        return streamTracks()
            .filter(t -> t.getBpm() >= min && t.getBpm() <= max)
            .collect(Collectors.toList());
    }

    /**
     * Cerca le tracce nella chiave musicale specificata (case-insensitive).
     * Salta le tracce che non hanno una chiave definita (key == null).
     */
    public List<Track> findTracksByKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("La chiave non puo' essere null");
        }
        return streamTracks()
            .filter(t -> t.getKey() != null)
            .filter(t -> t.getKey().equalsIgnoreCase(key))
            .collect(Collectors.toList());
    }

    /**
     * Cerca le tracce di un certo genere (case-insensitive).
     * Salta le tracce che non hanno un genere definito (genre == null).
     */
    public List<Track> findTracksByGenre(String genre) {
        if (genre == null) {
            throw new IllegalArgumentException("Il genere non puo' essere null");
        }
        return streamTracks()
            .filter(t -> t.getGenre() != null)
            .filter(t -> t.getGenre().equalsIgnoreCase(genre))
            .collect(Collectors.toList());
    }

    /**
     * Calcola il BPM medio delle tracce contenute.
     * Ritorna un OptionalDouble vuoto se la collezione e' vuota.
     */
    public OptionalDouble getAverageBpm() {
        return streamTracks()
            .mapToInt(Track::getBpm)
            .average();
    }

    /**
     * Raggruppa le tracce per genere. Le tracce senza genere finiscono
     * sotto la chiave "Sconosciuto".
     *
     * @return mappa genere -> lista di tracce di quel genere
     */
    public Map<String, List<Track>> getTracksGroupedByGenre() {
        return streamTracks()
            .collect(Collectors.groupingBy(
                t -> t.getGenre() == null ? "Sconosciuto" : t.getGenre()
            ));
    }

    // ===== Pattern Strategy (bonus) =====

    /**
     * Ordina tutte le tracce della collezione (incluse quelle annidate)
     * secondo la strategia fornita.
     *
     * Pattern Strategy: il criterio di ordinamento e' un parametro
     * intercambiabile a runtime, non hardcoded.
     *
     * @param strategy la strategia di ordinamento da applicare
     * @return nuova lista ordinata secondo la strategia
     */
    public List<Track> sortBy(SortStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("La strategia di ordinamento non puo' essere null");
        }
        List<Track> allTracks = streamTracks().collect(Collectors.toList());
        return strategy.sort(allTracks);
    }

    // ===== Pattern Memento (bonus) =====

    /**
     * Memento: snapshot immutabile dello stato di una Collection.
     *
     * Classe statica annidata: solo Collection ha accesso al costruttore
     * e al metodo getter (package-private), preservando l'encapsulation.
     * Il Caretaker (LibraryHistory) puo' passarsi memento ma non puo'
     * guardarci dentro.
     *
     * Effettua deep copy delle sotto-Collection: una modifica successiva
     * di una sotto-collezione non influenza lo snapshot.
     * Le Track non vengono copiate perche' immutabili (safe shallow copy).
     */
    public static class CollectionMemento {

        private final String savedName;
        private final List<LibraryNode> savedChildren;

        // Costruttore package-private: solo Collection puo' istanziare
        CollectionMemento(String name, List<LibraryNode> children) {
            this.savedName = name;
            // Deep copy: sotto-Collection clonate, Track passate per riferimento (immutabili)
            this.savedChildren = new ArrayList<>();
            for (LibraryNode child : children) {
                this.savedChildren.add(deepCopyNode(child));
            }
        }

        private static LibraryNode deepCopyNode(LibraryNode node) {
            if (node instanceof Track) {
                return node;   // Track immutabili: shallow copy sicura
            }
            if (node instanceof Collection) {
                Collection c = (Collection) node;
                Collection copy = new Collection(c.name);
                for (LibraryNode child : c.children) {
                    copy.children.add(deepCopyNode(child));
                }
                return copy;
            }
            throw new IllegalStateException("Tipo di nodo non supportato per la copia");
        }

        // Getter package-private: solo Collection puo' leggere
        String getSavedName() { return savedName; }
        List<LibraryNode> getSavedChildren() { return savedChildren; }
    }

    /**
     * Crea uno snapshot immutabile dello stato attuale della Collection.
     * Il Memento e' opaco: solo questa classe puo' interpretarlo.
     */
    public CollectionMemento saveMemento() {
        return new CollectionMemento(this.name, this.children);
    }

    /**
     * Ripristina lo stato della Collection a partire da un Memento precedente.
     * Sostituisce completamente i figli attuali con quelli salvati nello snapshot.
     *
     * @param memento il memento da cui ripristinare
     * @throws IllegalArgumentException se memento e' null o appartiene a un'altra Collection
     */
    public void restoreFromMemento(CollectionMemento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("Il memento non puo' essere null");
        }
        if (!this.name.equals(memento.getSavedName())) {
            throw new IllegalArgumentException(
                "Il memento appartiene a una Collection diversa (" +
                memento.getSavedName() + " vs " + this.name + ")");
        }
        this.children.clear();
        // Deep copy in ingresso per isolare lo snapshot da modifiche future
        for (LibraryNode child : memento.getSavedChildren()) {
            this.children.add(CollectionMemento.deepCopyNode(child));
        }
    }
}