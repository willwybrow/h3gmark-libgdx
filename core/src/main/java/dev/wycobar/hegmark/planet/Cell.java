package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.feature.ResolvedFeatureValue;
import dev.wycobar.hegmark.feature.StoredFeature;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Cell {
    private final Planet planet;
    private final CellId id;

    Cell(Planet planet, CellId id) {
        this.planet = Objects.requireNonNull(planet, "planet");
        this.id = Objects.requireNonNull(id, "id");
    }

    public Planet planet() {
        return planet;
    }

    public CellId id() {
        return id;
    }

    public int resolution() {
        return planet.grid().resolution(id);
    }

    public PlanetLatLon center() {
        return planet.grid().center(id);
    }

    public CellGeometry geometry() {
        return planet.grid().geometry(id);
    }

    public Optional<Cell> parent() {
        return resolution() == 0 ? Optional.empty() : Optional.of(parent(resolution() - 1));
    }

    public Cell parent(int resolution) {
        if (resolution < 0 || resolution >= resolution()) {
            throw new IllegalArgumentException("Parent resolution must be coarser than the cell");
        }
        return planet.cell(planet.grid().parent(id, resolution));
    }

    public List<Cell> children() {
        return resolution() == planet.grid().maximumResolution()
            ? List.of()
            : planet.grid().children(id, resolution() + 1).stream().map(planet::cell).toList();
    }

    public List<Cell> neighbors() {
        return planet.grid().neighbors(id).stream().map(planet::cell).toList();
    }

    public boolean contains(CellId candidate) {
        if (candidate.equals(id)) return true;
        int candidateResolution = planet.grid().resolution(candidate);
        return candidateResolution > resolution()
            && planet.grid().parent(candidate, resolution()).equals(id);
    }

    public boolean strictlyContains(CellId candidate) {
        return !candidate.equals(id) && contains(candidate);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Cell cell && planet == cell.planet && id.equals(cell.id);
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(planet) + id.hashCode();
    }

    @Override
    public String toString() {
        return "Cell[" + id.asHexString() + "]";
    }
}
