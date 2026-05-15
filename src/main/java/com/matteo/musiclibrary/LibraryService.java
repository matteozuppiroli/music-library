package com.matteo.musiclibrary;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Boundary del sistema: facade dei casi d'uso della libreria musicale.
 *
 * Tutti i metodi pubblici di questa classe applicano Exception Shielding:
 *  - catturano qualunque eccezione (di dominio o tecnica)
 *  - la loggano internamente con stack trace completo (livello SEVERE per tecniche,
 *    WARNING per quelle di dominio attese)
 *  - traducono l'errore in un Result&lt;T&gt; con messaggio user-friendly
 *
 * Il client non vede MAI stack trace, dettagli implementativi, o exception propagate.
 */
public class LibraryService {

    private static final Logger LOGGER = Logger.getLogger(LibraryService.class.getName());

    /**
     * Crea una Track dal payload fornito, applicando validazione e shielding.
     */
    public Result<Track> createTrack(String title, String artist, int bpm,
                                     String key, int durationSeconds, String genre) {
        try {
            Track t = LibraryNodeFactory.createTrack(title, artist, bpm, key, durationSeconds, genre);
            return Result.success(t);
        } catch (IllegalArgumentException e) {
            // Errore di validazione "atteso": logging meno severo
            LOGGER.log(Level.WARNING, "Validazione fallita in createTrack: " + e.getMessage(), e);
            return Result.failure("Dati della traccia non validi: " + e.getMessage());
        } catch (Exception e) {
            // Qualunque altra eccezione e' inattesa: log SEVERE + messaggio generico
            LOGGER.log(Level.SEVERE, "Errore inatteso in createTrack", e);
            return Result.failure("Impossibile creare la traccia, riprovare piu' tardi");
        }
    }

    /**
     * Crea una Collection vuota, applicando validazione e shielding.
     */
    public Result<Collection> createCollection(String name) {
        try {
            Collection c = LibraryNodeFactory.createCollection(name);
            return Result.success(c);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validazione fallita in createCollection: " + e.getMessage(), e);
            return Result.failure("Nome della collezione non valido: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore inatteso in createCollection", e);
            return Result.failure("Impossibile creare la collezione, riprovare piu' tardi");
        }
    }

    /**
     * Aggiunge un nodo a una collezione, applicando shielding.
     * Cattura sia eccezioni di dominio (duplicato) sia errori di validazione.
     */
    public Result<Collection> addNode(Collection target, LibraryNode node) {
        try {
            if (target == null) {
                return Result.failure("Collezione di destinazione non specificata");
            }
            target.add(node);
            return Result.success(target);
        } catch (DuplicateNodeException e) {
            // Errore di dominio atteso: livello INFO
            LOGGER.log(Level.INFO, "Duplicato rilevato: " + e.getMessage());
            return Result.failure(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validazione fallita in addNode: " + e.getMessage(), e);
            return Result.failure("Impossibile aggiungere il nodo: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore inatteso in addNode", e);
            return Result.failure("Operazione fallita, riprovare piu' tardi");
        }
    }
}