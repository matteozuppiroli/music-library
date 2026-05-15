package com.matteo.musiclibrary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy che ordina le tracce per durata crescente (in secondi).
 */
public class SortByDuration implements SortStrategy {

    @Override
    public List<Track> sort(List<Track> tracks) {
        if (tracks == null) {
            throw new IllegalArgumentException("La lista da ordinare non puo' essere null");
        }
        List<Track> copy = new ArrayList<>(tracks);
        copy.sort(Comparator.comparingInt(Track::getDurationSeconds));
        return copy;
    }

    @Override
    public String getName() {
        return "Durata crescente";
    }
}