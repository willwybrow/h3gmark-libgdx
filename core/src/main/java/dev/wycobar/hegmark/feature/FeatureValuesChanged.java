package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

import java.util.Set;

public record FeatureValuesChanged(
    StoredFeature<?> feature,
    Set<Cell> changedCells,
    Set<Cell> removedCells
) {
    public FeatureValuesChanged {
        changedCells = Set.copyOf(changedCells);
        removedCells = Set.copyOf(removedCells);
        if (changedCells.isEmpty() && removedCells.isEmpty()) {
            throw new IllegalArgumentException("A feature change must contain at least one changed cell");
        }
    }
}
