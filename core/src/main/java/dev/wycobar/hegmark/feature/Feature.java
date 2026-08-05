package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.Layer;

import java.util.Optional;

interface Feature<T> {
    String id();

    String name();

    ResolutionRange viewableRange();

    Optional<ResolutionRange> settableRange();

    T valueAt(Cell cell);

    default boolean isViewableAt(Layer resolution) {
        return viewableRange().contains(resolution);
    }

    default boolean isSettableAt(Layer resolution) {
        return settableRange().map(range -> range.contains(resolution)).orElse(false);
    }
}
