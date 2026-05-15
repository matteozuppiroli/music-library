package com.matteo.musiclibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository per il salvataggio e il caricamento della libreria su disco
 * in formato JSON.
 *
 * Applica Exception Shielding: tutte le IOException vengono catturate,
 * loggate, e tradotte in Result&lt;T&gt; con messaggio user-friendly.
 *
 * Tecnologie usate:
 *  - Java I/O moderno (java.nio.file.Files, Path)
 *  - Serializzazione JSON custom (JsonSerializer / JsonDeserializer)
 *  - Logging strutturato (java.util.logging)
 */
public class LibraryRepository {

    private static final Logger LOGGER = Logger.getLogger(LibraryRepository.class.getName());

    /**
     * Salva un LibraryNode (tipicamente la Collection radice) in un file JSON.
     *
     * @param node il nodo da salvare
     * @param filePath percorso del file di destinazione
     * @return Result.success(path) se il salvataggio e' andato a buon fine
     */
    public Result<String> save(LibraryNode node, String filePath) {
        try {
            if (node == null) {
                return Result.failure("Nessun nodo specificato per il salvataggio");
            }
            if (filePath == null || filePath.isBlank()) {
                return Result.failure("Percorso del file non specificato");
            }

            String json = JsonSerializer.toJson(node);
            Path path = Paths.get(filePath);
            Files.writeString(path, json);

            LOGGER.log(Level.INFO, "Libreria salvata su " + path.toAbsolutePath());
            return Result.success(path.toAbsolutePath().toString());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore I/O durante il salvataggio", e);
            return Result.failure("Impossibile salvare il file: errore di scrittura");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore inatteso durante il salvataggio", e);
            return Result.failure("Salvataggio fallito, riprovare piu' tardi");
        }
    }

    /**
     * Carica un LibraryNode da un file JSON.
     */
    public Result<LibraryNode> load(String filePath) {
        try {
            if (filePath == null || filePath.isBlank()) {
                return Result.failure("Percorso del file non specificato");
            }

            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return Result.failure("File non trovato: " + filePath);
            }

            String json = Files.readString(path);
            LibraryNode node = JsonDeserializer.fromJson(json);

            LOGGER.log(Level.INFO, "Libreria caricata da " + path.toAbsolutePath());
            return Result.success(node);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore I/O durante il caricamento", e);
            return Result.failure("Impossibile leggere il file: errore di lettura");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "File JSON malformato: " + e.getMessage(), e);
            return Result.failure("Il file non e' un JSON valido per la libreria");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore inatteso durante il caricamento", e);
            return Result.failure("Caricamento fallito, riprovare piu' tardi");
        }
    }
}