package com.matteo.musiclibrary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResultTest {

    @Test
    void success_marca_risultato_come_riuscito() {
        Result<String> r = Result.success("hello");

        assertTrue(r.isSuccess());
        assertFalse(r.isFailure());
        assertEquals("hello", r.getValue());
    }

    @Test
    void failure_marca_risultato_come_fallito() {
        Result<String> r = Result.failure("errore generico");

        assertFalse(r.isSuccess());
        assertTrue(r.isFailure());
        assertEquals("errore generico", r.getError());
    }

    @Test
    void getValue_su_risultato_fallito_lancia_eccezione() {
        Result<String> r = Result.failure("errore");

        assertThrows(IllegalStateException.class, () -> r.getValue());
    }

    @Test
    void getError_su_risultato_riuscito_lancia_eccezione() {
        Result<String> r = Result.success("ok");

        assertThrows(IllegalStateException.class, () -> r.getError());
    }

    @Test
    void success_con_valore_nullo_lancia_eccezione() {
        assertThrows(NullPointerException.class, () -> Result.success(null));
    }

    @Test
    void failure_con_messaggio_nullo_lancia_eccezione() {
        assertThrows(NullPointerException.class, () -> Result.failure(null));
    }

    @Test
    void failure_con_messaggio_vuoto_lancia_eccezione() {
        assertThrows(IllegalArgumentException.class, () -> Result.failure("   "));
    }
}