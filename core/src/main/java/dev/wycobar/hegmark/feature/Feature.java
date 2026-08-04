package dev.wycobar.hegmark.feature;

import java.util.Optional;

public interface Feature<T> {
    String id();

    String name();

    Class<T> valueType();

    ResolutionRange viewableRange();

    Optional<ResolutionRange> settableRange();

    default boolean isViewableAt(int resolution) {
        return viewableRange().contains(resolution);
    }

    default boolean isSettableAt(int resolution) {
        return settableRange().map(range -> range.contains(resolution)).orElse(false);
    }
}
