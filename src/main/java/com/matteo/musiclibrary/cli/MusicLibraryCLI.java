package com.matteo.musiclibrary.cli;

import com.matteo.musiclibrary.Collection;
import com.matteo.musiclibrary.LibraryHistory;
import com.matteo.musiclibrary.LibraryNode;
import com.matteo.musiclibrary.LibraryRepository;
import com.matteo.musiclibrary.LibraryService;
import com.matteo.musiclibrary.Result;
import com.matteo.musiclibrary.SortByArtist;
import com.matteo.musiclibrary.SortByBpm;
import com.matteo.musiclibrary.SortByDuration;
import com.matteo.musiclibrary.SortByTitle;
import com.matteo.musiclibrary.SortStrategy;
import com.matteo.musiclibrary.Track;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Scanner;

/**
 * Interfaccia a riga di comando per Music Library.
 *
 * Thin wrapper sopra LibraryService: si occupa solo di parsing degli
 * input testuali e formattazione degli output. Tutta la logica di
 * business resta nei layer sottostanti.
 *
 * Riusa Result&lt;T&gt; del LibraryService per la gestione errori,
 * ereditando automaticamente l'Exception Shielding.
 *
 * Mantiene un LibraryHistory locale per supportare undo/redo.
 */
public class MusicLibraryCLI {

    private final LibraryService service = new LibraryService();
    private final LibraryRepository repository = new LibraryRepository();
    private final LibraryHistory history = new LibraryHistory();
    private Collection current;
    private boolean running = true;

    public static void main(String[] args) {
        new MusicLibraryCLI().run();
    }

    public void run() {
        printWelcome();
        Scanner scanner = new Scanner(System.in);

        while (running) {
            System.out.print(prompt());
            if (!scanner.hasNextLine()) break;

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            try {
                executeCommand(line);
            } catch (Exception e) {
                // Estrema rete di sicurezza: nessuna eccezione interna deve mai raggiungere l'utente
                System.out.println("Errore inatteso. Riprovare.");
            }
        }

        System.out.println("Arrivederci!");
        scanner.close();
    }

    private String prompt() {
        if (current == null) return "music-library> ";
        return "music-library [" + current.getName() + "]> ";
    }

    private void executeCommand(String line) {
        String[] parts = line.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "help":              handleHelp(); break;
            case "create-collection": handleCreateCollection(args); break;
            case "use":               handleUse(args); break;
            case "create-track":      handleCreateTrack(args); break;
            case "list":              handleList(); break;
            case "filter-bpm":        handleFilterBpm(args); break;
            case "filter-genre":      handleFilterGenre(args); break;
            case "sort":              handleSort(args); break;
            case "stats":             handleStats(); break;
            case "save":              handleSave(args); break;
            case "load":              handleLoad(args); break;
            case "undo":              handleUndo(); break;
            case "redo":              handleRedo(); break;
            case "exit":
            case "quit":              running = false; break;
            default:                  System.out.println("Comando sconosciuto: " + command + ". Digita 'help' per la lista dei comandi.");
        }
    }

    // ===== Handler dei comandi =====

    private void handleHelp() {
        System.out.println();
        System.out.println("Comandi disponibili:");
        System.out.println("  help                                            mostra questo aiuto");
        System.out.println("  create-collection <nome>                        crea una nuova collezione e la rende corrente");
        System.out.println("  use <nome>                                      cambia la collezione corrente (deve esistere)");
        System.out.println("  create-track <titolo> <artista> <bpm> <key> <durata> <genere>");
        System.out.println("                                                  crea una traccia e la aggiunge alla collezione corrente");
        System.out.println("                                                  (i valori multi-parola vanno tra virgolette, es. \"Brown Sugar\")");
        System.out.println("  list                                            mostra tutte le tracce della collezione corrente");
        System.out.println("  filter-bpm <min> <max>                          filtra le tracce per range di BPM");
        System.out.println("  filter-genre <genere>                           filtra le tracce per genere");
        System.out.println("  sort <bpm|title|artist|duration>                ordina le tracce secondo il criterio");
        System.out.println("  stats                                           statistiche della collezione corrente");
        System.out.println("  save <file>                                     salva la collezione corrente su file JSON");
        System.out.println("  load <file>                                     carica una collezione da file JSON");
        System.out.println("  undo                                            annulla l'ultima operazione");
        System.out.println("  redo                                            rifa' l'ultima operazione annullata");
        System.out.println("  exit                                            esci");
        System.out.println();
    }

    private void handleCreateCollection(String args) {
        if (args.isEmpty()) {
            System.out.println("Uso: create-collection <nome>");
            return;
        }
        Result<Collection> r = service.createCollection(args);
        if (r.isSuccess()) {
            current = r.getValue();
            history.clear();
            System.out.println("✓ Creata collezione: " + current.getName());
        } else {
            System.out.println("✗ " + r.getError());
        }
    }

    private void handleUse(String args) {
        System.out.println("Per ora la CLI gestisce una sola collezione alla volta in memoria.");
        System.out.println("Usa 'load <file>' per caricare una collezione esistente da disco.");
    }

    private void handleCreateTrack(String args) {
        if (current == null) {
            System.out.println("✗ Nessuna collezione corrente. Usa 'create-collection <nome>' o 'load <file>' prima.");
            return;
        }

        String[] tokens = parseArgsWithQuotes(args);
        if (tokens.length != 6) {
            System.out.println("Uso: create-track <titolo> <artista> <bpm> <key> <durata> <genere>");
            System.out.println("Esempio: create-track \"Brown Sugar\" \"D'Angelo\" 93 Eb 280 R&B");
            return;
        }

        try {
            String title = tokens[0];
            String artist = tokens[1];
            int bpm = Integer.parseInt(tokens[2]);
            String key = tokens[3];
            int duration = Integer.parseInt(tokens[4]);
            String genre = tokens[5];

            Result<Track> trackResult = service.createTrack(title, artist, bpm, key, duration, genre);
            if (!trackResult.isSuccess()) {
                System.out.println("✗ " + trackResult.getError());
                return;
            }

            history.saveSnapshot(current);
            Result<Collection> addResult = service.addNode(current, trackResult.getValue());
            if (addResult.isSuccess()) {
                System.out.println("✓ Aggiunta: " + trackResult.getValue().getTitle() + " — " + trackResult.getValue().getArtist());
            } else {
                System.out.println("✗ " + addResult.getError());
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ BPM e durata devono essere numeri interi");
        }
    }

    private void handleList() {
        if (current == null) {
            System.out.println("Nessuna collezione corrente.");
            return;
        }
        System.out.println();
        System.out.println("Collezione: " + current.getName() + " (" + current.getTrackCount() + " tracce)");
        if (current.getTrackCount() == 0) {
            System.out.println("  (vuota)");
        } else {
            int i = 1;
            for (Track t : current) {
                printTrack(i++, t);
            }
        }
        System.out.println();
    }

    private void handleFilterBpm(String args) {
        if (current == null) { System.out.println("Nessuna collezione corrente."); return; }
        String[] tokens = args.split("\\s+");
        if (tokens.length != 2) {
            System.out.println("Uso: filter-bpm <min> <max>");
            return;
        }
        try {
            int min = Integer.parseInt(tokens[0]);
            int max = Integer.parseInt(tokens[1]);
            List<Track> tracks = current.findTracksByBpmRange(min, max);
            printTrackList("Tracce con BPM tra " + min + " e " + max, tracks);
        } catch (NumberFormatException e) {
            System.out.println("✗ I valori di BPM devono essere numeri interi");
        } catch (IllegalArgumentException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void handleFilterGenre(String args) {
        if (current == null) { System.out.println("Nessuna collezione corrente."); return; }
        if (args.isEmpty()) {
            System.out.println("Uso: filter-genre <genere>");
            return;
        }
        try {
            List<Track> tracks = current.findTracksByGenre(args);
            printTrackList("Tracce di genere '" + args + "'", tracks);
        } catch (IllegalArgumentException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void handleSort(String args) {
        if (current == null) { System.out.println("Nessuna collezione corrente."); return; }
        SortStrategy strategy;
        switch (args.toLowerCase()) {
            case "bpm":      strategy = new SortByBpm(); break;
            case "title":    strategy = new SortByTitle(); break;
            case "artist":   strategy = new SortByArtist(); break;
            case "duration": strategy = new SortByDuration(); break;
            default:
                System.out.println("Criterio sconosciuto. Disponibili: bpm, title, artist, duration");
                return;
        }
        List<Track> sorted = current.sortBy(strategy);
        printTrackList("Tracce ordinate per " + strategy.getName(), sorted);
    }

    private void handleStats() {
        if (current == null) { System.out.println("Nessuna collezione corrente."); return; }
        System.out.println();
        System.out.println("Statistiche di '" + current.getName() + "':");
        System.out.println("  Tracce totali:  " + current.getTrackCount());
        System.out.println("  Durata totale:  " + formatDuration(current.getTotalDurationSeconds()));
        OptionalDouble avg = current.getAverageBpm();
        if (avg.isPresent()) {
            System.out.printf("  BPM medio:      %.1f%n", avg.getAsDouble());
        }
        Map<String, List<Track>> byGenre = current.getTracksGroupedByGenre();
        if (!byGenre.isEmpty()) {
            System.out.println("  Per genere:");
            for (Map.Entry<String, List<Track>> entry : byGenre.entrySet()) {
                System.out.println("    " + entry.getKey() + ": " + entry.getValue().size());
            }
        }
        System.out.println();
    }

    private void handleSave(String args) {
        if (current == null) { System.out.println("Nessuna collezione corrente."); return; }
        if (args.isEmpty()) {
            System.out.println("Uso: save <file>");
            return;
        }
        Result<String> r = repository.save(current, args);
        if (r.isSuccess()) {
            System.out.println("✓ Salvato su: " + r.getValue());
        } else {
            System.out.println("✗ " + r.getError());
        }
    }

    private void handleLoad(String args) {
        if (args.isEmpty()) {
            System.out.println("Uso: load <file>");
            return;
        }
        Result<LibraryNode> r = repository.load(args);
        if (r.isSuccess()) {
            if (r.getValue() instanceof Collection) {
                current = (Collection) r.getValue();
                history.clear();
                System.out.println("✓ Caricata collezione '" + current.getName() + "' con " + current.getTrackCount() + " tracce");
            } else {
                System.out.println("✗ Il file contiene una traccia singola, non una collezione");
            }
        } else {
            System.out.println("✗ " + r.getError());
        }
    }

    private void handleUndo() {
        if (current == null) { System.out.println("Nessuna collezione corrente."); return; }
        if (history.undo(current)) {
            System.out.println("✓ Operazione annullata");
        } else {
            System.out.println("Nessuna operazione da annullare");
        }
    }

    private void handleRedo() {
        if (current == null) { System.out.println("Nessuna collezione corrente."); return; }
        if (history.redo(current)) {
            System.out.println("✓ Operazione rifatta");
        } else {
            System.out.println("Nessuna operazione da rifare");
        }
    }

    // ===== Helper di formattazione =====

    private void printWelcome() {
        System.out.println();
        System.out.println("=================================================");
        System.out.println("   🎵  Music Library CLI v1.0");
        System.out.println("=================================================");
        System.out.println("Digita 'help' per la lista dei comandi.");
        System.out.println("Digita 'exit' per uscire.");
        System.out.println();
    }

    private void printTrack(int index, Track t) {
        String key = t.getKey() != null ? t.getKey() : "-";
        String genre = t.getGenre() != null ? t.getGenre() : "-";
        System.out.printf("  %2d. %-30s %-25s %3d BPM  %-4s  %s  [%s]%n",
            index, truncate(t.getTitle(), 30), truncate(t.getArtist(), 25),
            t.getBpm(), key, formatDuration(t.getDurationSeconds()), genre);
    }

    private void printTrackList(String header, List<Track> tracks) {
        System.out.println();
        System.out.println(header + " (" + tracks.size() + " trovate):");
        if (tracks.isEmpty()) {
            System.out.println("  (nessun risultato)");
        } else {
            int i = 1;
            for (Track t : tracks) {
                printTrack(i++, t);
            }
        }
        System.out.println();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Parsing minimale di stringa con argomenti che possono essere
     * racchiusi tra virgolette (per supportare titoli e nomi con spazi).
     */
    private String[] parseArgsWithQuotes(String input) {
        List<String> tokens = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens.toArray(new String[0]);
    }
}