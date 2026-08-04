package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

import java.util.Optional;
import java.util.Set;

public interface StoredFeature<T> extends Feature<T> {
    T generatedValue(Cell cell);

    default void validateValue(T value) {
    }

    default int algorithmVersion() {
        return 1;
    }

    default Optional<T> aggregate(Cell cell) {
        return Optional.empty();
    }

    default Set<Cell> affectedAggregationCells(FeatureValuesChanged event) {
        return Set.of();
    }
}
