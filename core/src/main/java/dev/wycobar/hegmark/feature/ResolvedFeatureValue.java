package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

import java.util.Optional;

public record ResolvedFeatureValue<T>(
    boolean applicable,
    boolean directlyEditable,
    Optional<T> storedValue,
    T effectiveValue,
    T displayValue,
    FeatureValueSource effectiveSource,
    FeatureValueSource displaySource,
    Optional<Cell> sourceCell
) {
}
