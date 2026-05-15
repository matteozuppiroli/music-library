package com.matteo.musiclibrary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LibraryConfigTest {

    @AfterEach
    void resetSingleton() {
        // Pulisce lo stato del singleton dopo ogni test
        LibraryConfig.resetForTesting();
    }

    @Test
    void getInstance_ritorna_un_oggetto_non_null() {
        LibraryConfig config = LibraryConfig.getInstance();
        assertNotNull(config);
    }

    @Test
    void getInstance_ritorna_sempre_la_stessa_istanza() {
        LibraryConfig first = LibraryConfig.getInstance();
        LibraryConfig second = LibraryConfig.getInstance();

        // assertSame verifica che siano ESATTAMENTE lo stesso oggetto in memoria
        assertSame(first, second);
    }

    @Test
    void valori_di_default_sono_quelli_attesi() {
        LibraryConfig config = LibraryConfig.getInstance();

        assertEquals(40, config.getMinBpm());
        assertEquals(300, config.getMaxBpm());
        assertEquals(200, config.getMaxNameLength());
        assertNotNull(config.getDefaultSaveDirectory());
    }

    @Test
    void setDefaultSaveDirectory_persiste_il_valore() {
        LibraryConfig config = LibraryConfig.getInstance();
        config.setDefaultSaveDirectory("/tmp/test-libraries");

        // Lo stesso Singleton riletto deve vedere il valore aggiornato
        assertEquals("/tmp/test-libraries",
                     LibraryConfig.getInstance().getDefaultSaveDirectory());
    }

    @Test
    void setDefaultSaveDirectory_rifiuta_stringa_vuota() {
        LibraryConfig config = LibraryConfig.getInstance();
        assertThrows(IllegalArgumentException.class,
                     () -> config.setDefaultSaveDirectory(""));
    }

    @Test
    void setDefaultSaveDirectory_rifiuta_null() {
        LibraryConfig config = LibraryConfig.getInstance();
        assertThrows(IllegalArgumentException.class,
                     () -> config.setDefaultSaveDirectory(null));
    }

    // ===== Integrazione: Track usa LibraryConfig per la validazione =====

    @Test
    void Track_usa_il_BPM_range_dal_singleton() {
        // Verifico che la validazione di Track legga da LibraryConfig
        // Il range default e' 40-300, quindi 39 e 301 devono fallire

        assertThrows(IllegalArgumentException.class, () ->
            new Track("Test", "Artist", 39, "C", 200, "R&B"));

        assertThrows(IllegalArgumentException.class, () ->
            new Track("Test", "Artist", 301, "C", 200, "R&B"));

        // 40 e 300 invece sono inclusi e validi
        new Track("Test", "Artist", 40, "C", 200, "R&B");
        new Track("Test", "Artist", 300, "C", 200, "R&B");
    }
}