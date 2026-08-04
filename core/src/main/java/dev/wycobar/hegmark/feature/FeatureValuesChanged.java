package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

import java.util.Set;

public record FeatureValuesChanged(
    StoredFeature<?> feature,
    FeatureMutationOperation operation,
    Set<Cell> changedCells,
    Set<Cell> removedDescendants
) {
    public FeatureValuesChanged {
        changedCells = Set.copyOf(changedCells);
        removedDescendants = Set.copyOf(removedDescendants);
        if (changedCells.isEmpty() && removedDescendants.isEmpty()) {
            throw new IllegalArgumentException("A feature change must contain at least one changed cell");
        }
    }
}
