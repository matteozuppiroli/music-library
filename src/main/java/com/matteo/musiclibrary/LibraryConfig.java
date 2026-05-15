package com.matteo.musiclibrary;

/**
 * Configurazione globale dell'applicazione. Pattern Singleton.
 *
 * Espone parametri coerenti per tutta l'app: cartella di default per
 * il salvataggio, range di validazione, costanti del dominio.
 *
 * Implementazione: lazy initialization con double-checked locking
 * per garantire thread-safety nella creazione dell'istanza unica.
 *
 * Nota didattica: Singleton e' spesso considerato un anti-pattern quando
 * applicato a sproposito perche' introduce stato globale e complica
 * il testing. Qui l'uso e' limitato a configurazione di sola lettura,
 * quindi gli inconvenienti tipici non si manifestano.
 */
public final class LibraryConfig {

    // Unica istanza, volatile per garantire visibilita' tra thread
    private static volatile LibraryConfig instance;

    // Parametri di configurazione
    private String defaultSaveDirectory = "./libraries";
    private int minBpm = 40;
    private int maxBpm = 300;
    private int maxNameLength = 200;

    /**
     * Costruttore privato: impedisce istanziazione esterna.
     */
    private LibraryConfig() {
        // Solo la classe stessa puo' costruirsi
    }

    /**
     * Ritorna l'unica istanza di LibraryConfig.
     * Thread-safe via double-checked locking.
     */
    public static LibraryConfig getInstance() {
        if (instance == null) {
            synchronized (LibraryConfig.class) {
                if (instance == null) {
                    instance = new LibraryConfig();
                }
            }
        }
        return instance;
    }

    // ===== Getter =====

    public String getDefaultSaveDirectory() {
        return this.defaultSaveDirectory;
    }

    public int getMinBpm() {
        return this.minBpm;
    }

    public int getMaxBpm() {
        return this.maxBpm;
    }

    public int getMaxNameLength() {
        return this.maxNameLength;
    }

    // ===== Setter (uso amministrativo) =====

    public void setDefaultSaveDirectory(String directory) {
        if (directory == null || directory.isBlank()) {
            throw new IllegalArgumentException("La cartella non puo' essere vuota");
        }
        this.defaultSaveDirectory = directory;
    }

    /**
     * Reset dello stato della configurazione (utile per i test).
     * Solo package-private: non esposto all'esterno.
     */
    static void resetForTesting() {
        instance = null;
    }
}