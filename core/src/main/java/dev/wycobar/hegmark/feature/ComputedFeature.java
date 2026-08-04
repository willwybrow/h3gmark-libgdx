package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

import java.util.Optional;

public interface ComputedFeature<T> extends Feature<T> {
    T compute(Cell cell);

    @Override
    default Optional<ResolutionRange> settableRange() {
        return Optional.empty();
    }
}
