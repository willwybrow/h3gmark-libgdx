package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.feature.Feature;
import dev.wycobar.hegmark.feature.ExplicitFeatureValue;
import dev.wycobar.hegmark.feature.ResolvedFeatureValue;
import dev.wycobar.hegmark.feature.StoredFeature;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Cell {
    private static final int MAX_LOCAL_DISK_RADIUS = 100;
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

    public boolean isPentagon() {
        return planet.grid().isPentagon(id);
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

    public List<Cell> disk(int radius) {
        if (radius < 0 || radius > MAX_LOCAL_DISK_RADIUS) {
            throw new IllegalArgumentException("Cell disk radius must be between 0 and " + MAX_LOCAL_DISK_RADIUS);
        }
        return planet.grid().disk(id, radius).stream().map(planet::cell).toList();
    }

    public List<ExplicitFeatureValue<?>> explicitFeatures() {
        return planet.explicitValues().values(id);
    }

    public <T> Optional<T> explicitFeature(StoredFeature<T> feature) {
        return planet.explicitValue(this, feature);
    }

    public <T> ResolvedFeatureValue<T> resolvedFeature(StoredFeature<T> feature) {
        return planet.resolvedValue(this, feature);
    }

    public <T> T feature(Feature<T> feature) {
        return planet.value(this, feature);
    }

    public boolean hasExplicitDescendant(StoredFeature<?> feature) {
        return planet.hasExplicitDescendant(this, feature);
    }

    public int explicitDescendantCount(StoredFeature<?> feature) {
        return planet.explicitDescendantCount(this, feature);
    }

    public List<Cell> ancestorsIncludingSelf(int minimumResolution) {
        if (minimumResolution < 0 || minimumResolution > resolution()) {
            throw new IllegalArgumentException("Minimum ancestor resolution is outside the cell hierarchy");
        }
        java.util.ArrayList<Cell> ancestors = new java.util.ArrayList<>();
        Cell current = this;
        while (true) {
            ancestors.add(current);
            if (current.resolution() == minimumResolution) return List.copyOf(ancestors);
            current = current.parent().orElseThrow();
        }
    }

    public long stableSeed(String featureId, int algorithmVersion) {
        return planet.grid().stableSeed(id, planet.definition().worldSeed(), featureId, algorithmVersion);
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
