package com.matteo.musiclibrary;

import java.util.Objects;

/**
 * Rappresenta l'esito di un'operazione: successo con valore, oppure fallimento
 * con messaggio di errore pulito (sicuro per l'utente).
 *
 * Veicolo del pattern Exception Shielding: al boundary del sistema le eccezioni
 * vengono catturate, loggate e tradotte in Result.failure(...). Il client lavora
 * con un valore di ritorno tipizzato invece che con eccezioni propagate.
 *
 * Ispirato a Result&lt;T&gt; di Rust e Kotlin.
 *
 * Classe immutabile e thread-safe per costruzione.
 *
 * @param <T> il tipo del valore in caso di successo
 */
public final class Result<T> {

    private final T value;
    private final String error;
    private final boolean success;

    private Result(T value, String error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    /**
     * Crea un Result che rappresenta un'operazione riuscita con un valore.
     */
    public static <T> Result<T> success(T value) {
        Objects.requireNonNull(value, "Il valore di un Result.success non puo' essere nullo");
        return new Result<>(value, null, true);
    }

    /**
     * Crea un Result che rappresenta un fallimento con messaggio di errore.
     * Il messaggio deve essere user-friendly e non contenere dettagli implementativi.
     */
    public static <T> Result<T> failure(String error) {
        Objects.requireNonNull(error, "Il messaggio di errore non puo' essere nullo");
        if (error.isBlank()) {
            throw new IllegalArgumentException("Il messaggio di errore non puo' essere vuoto");
        }
        return new Result<>(null, error, false);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public boolean isFailure() {
        return !this.success;
    }

    /**
     * @return il valore in caso di successo
     * @throws IllegalStateException se questo Result rappresenta un fallimento
     */
    public T getValue() {
        if (!this.success) {
            throw new IllegalStateException("Tentativo di leggere il valore da un Result fallito");
        }
        return this.value;
    }

    /**
     * @return il messaggio di errore in caso di fallimento
     * @throws IllegalStateException se questo Result rappresenta un successo
     */
    public String getError() {
        if (this.success) {
            throw new IllegalStateException("Tentativo di leggere l'errore da un Result riuscito");
        }
        return this.error;
    }
}