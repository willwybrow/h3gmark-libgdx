package dev.wycobar.hegmark.planet;

import java.util.List;
import java.util.Objects;

public record CellGeometry(
    CellId cellId,
    int resolution,
    PlanetLatLon center,
    List<PlanetLatLon> boundary,
    boolean pentagon
) {
    public CellGeometry {
        Objects.requireNonNull(cellId, "cellId");
        Objects.requireNonNull(center, "center");
        boundary = List.copyOf(boundary);
        if (boundary.size() < 3) throw new IllegalArgumentException("A cell boundary needs at least three vertices");
    }
}
