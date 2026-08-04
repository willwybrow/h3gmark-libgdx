package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.Optional;

public record ResolvedElevation(
    boolean applicable,
    boolean directlyEditable,
    double meters,
    ElevationSource source,
    Optional<CellId> sourceCell
) {
    public static ResolvedElevation notApplicable() {
        return new ResolvedElevation(false, false, 0.0, ElevationSource.NOT_APPLICABLE, Optional.empty());
    }
}
