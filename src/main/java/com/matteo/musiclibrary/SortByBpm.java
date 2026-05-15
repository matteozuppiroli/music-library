package com.matteo.musiclibrary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy che ordina le tracce per BPM crescente.
 */
public class SortByBpm implements SortStrategy {

    @Override
    public List<Track> sort(List<Track> tracks) {
        if (tracks == null) {
            throw new IllegalArgumentException("La lista da ordinare non puo' essere null");
        }
        List<Track> copy = new ArrayList<>(tracks);
        copy.sort(Comparator.comparingInt(Track::getBpm));
        return copy;
    }

    @Override
    public String getName() {
        return "BPM crescente";
    }
}