package com.matteo.musiclibrary;

public class Track implements LibraryNode {

    private String title;
    private String artist;
    private int bpm;
    private String key;
    private int durationSeconds;
    private String genre;

    /** 
     * Costruisce una nuova Track.
     * 
     * @param title     titolo (obbligatorio, non vuoto)
     * @param artist    artista (obbligatorio, non vuoto)
     * @param bpm       battiti per minuto (40-300)
     * @param key       chiave musicale (opzionale, può essere null)
     * @param durationSeconds       durata in secondi (positiva)
     * @param genre     genere (opzionale, può essere null)
     * @throws IllegalArgumentException     se uno dei parametri obbligatori non è valido
    */

    public Track(String title, String artist, int bpm, String key, int durationSeconds, String genre) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto");
        }
        if (artist == null || artist.isBlank()) {
            throw new IllegalArgumentException("L'artista non può essere vuoto");
        }
        LibraryConfig config = LibraryConfig.getInstance();
        if (bpm < config.getMinBpm() || bpm > config.getMaxBpm()) {
            throw new IllegalArgumentException(
        "Il BPM deve essere tra " + config.getMinBpm() + " e " + config.getMaxBpm());
}
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("La durata deve essere positiva");
        }

        this.title = title;
        this.artist = artist;
        this.bpm = bpm;
        this.key = key;
        this.durationSeconds = durationSeconds;
        this.genre = genre;
    }

    // ===== Getter =====

    public String getTitle(){
        return this.title;
    }

    public String getArtist(){
        return this.artist;
    }

    public int getBpm(){
        return this.bpm;
    }

    public String getKey(){
        return this.key;
    }

    public int getDurationSeconds(){
        return this.durationSeconds;
    }

    public String getGenre(){
        return this.genre;
    }

    // ===== Implementazione di LibraryNode (pattern Composite) =====

    @Override
    public String getName() {
        return this.title;
    }

    @Override
    public int getTrackCount() {
        return 1;
    }

    @Override
    public int getTotalDurationSeconds() {
        return this.durationSeconds;
    }

    // ===== Uguaglianza per nome del brano + artista =====

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Track)) return false;
        Track o = (Track) other;
        return this.title.equalsIgnoreCase(o.title)
            && this.artist.equalsIgnoreCase(o.artist);
    }

    @Override
    public int hashCode() {
        return (this.title.toLowerCase() + "|" + this.artist.toLowerCase()).hashCode();
    }

    // ===== Pattern Builder (bonus) =====

    /**
     * Builder per la costruzione fluente di Track.
     *
     * Pattern Builder: rende esplicita ogni campo, supporta parametri
     * opzionali senza overload del costruttore, e abilita una sintassi
     * fluente leggibile.
     *
     * I parametri obbligatori (title, artist) sono richiesti nel costruttore
     * del Builder, gli altri si aggiungono con metodi fluenti opzionali.
     *
     * Esempio d'uso:
     * <pre>
     * Track t = new Track.Builder("Brown Sugar", "D'Angelo")
     *     .bpm(93)
     *     .key("Eb")
     *     .durationSeconds(280)
     *     .genre("R&amp;B")
     *     .build();
     * </pre>
     */
    public static class Builder {

        // Obbligatori (passati nel costruttore del Builder)
        private final String title;
        private final String artist;

        // Opzionali (con valori di default sensati)
        private int bpm = 120;                  // default neutro
        private String key = null;
        private int durationSeconds = 180;      // default 3 minuti
        private String genre = null;

        /**
         * Crea un Builder con i parametri obbligatori.
         */
        public Builder(String title, String artist) {
            this.title = title;
            this.artist = artist;
        }

        public Builder bpm(int bpm) {
            this.bpm = bpm;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder durationSeconds(int durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder genre(String genre) {
            this.genre = genre;
            return this;
        }

        /**
         * Costruisce l'oggetto Track finale. La validazione e' delegata
         * al costruttore di Track, garantendo che le stesse regole siano
         * applicate sia in costruzione diretta sia tramite Builder.
         */
        public Track build() {
            return new Track(title, artist, bpm, key, durationSeconds, genre);
        }
    }
}