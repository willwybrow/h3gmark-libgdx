package dev.wycobar.hegmark.feature;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import dev.wycobar.hegmark.planet.Cell;

public final class ElevationFeature implements StoredFeature<Double>, ProvidedFeatures {
    public static final ElevationFeature INSTANCE = new ElevationFeature();

    private static final ResolutionRange VIEWABLE = new ResolutionRange(1, 8);
    private static final ResolutionRange SETTABLE = new ResolutionRange(2, 7);
    private final LandFeature landFeature = new LandFeature(this, new ResolutionRange(2, 8));
    private final ElevationGenerator generator = new ElevationGenerator();

    private ElevationFeature() {
    }

    @Override
    public String id() {
        return "elevation";
    }

    @Override
    public String name() {
        return "Elevation";
    }

    @Override
    public Class<Double> valueType() {
        return Double.class;
    }

    @Override
    public ResolutionRange viewableRange() {
        return VIEWABLE;
    }

    @Override
    public Optional<ResolutionRange> settableRange() {
        return Optional.of(SETTABLE);
    }

    @Override
    public Double generatedValue(Cell cell) {
        long seed = cell.stableSeed(id(), algorithmVersion());
        return generator.generate(cell.planet().definition(), seed, cell.center());
    }

    @Override
    public int algorithmVersion() {
        return ElevationGenerator.ALGORITHM_VERSION;
    }

    @Override
    public void validateValue(Double value) {
        if (value == null || !Double.isFinite(value)) {
            throw new IllegalArgumentException("Elevation must be finite");
        }
    }

    @Override
    public Optional<Double> aggregate(Cell parent) {
        if (!parent.hasExplicitDescendant(this)) return Optional.empty();
        int childResolution = parent.resolution() + 1;
        if (childResolution > settableRange().orElseThrow().maximum()) return Optional.empty();
        var children = parent.children();
        if (children.isEmpty()) return Optional.empty();
        return Optional.of(children.stream()
            .sorted(Comparator.comparing(child -> child.id().asHexString()))
            .mapToDouble(child -> child.feature(this))
            .average()
            .orElseThrow());
    }

    @Override
    public Set<Cell> affectedAggregationCells(FeatureValuesChanged event) {
        Set<Cell> affected = new HashSet<>();
        event.changedCells().forEach(cell -> addAncestors(cell, affected));
        event.removedDescendants().forEach(cell -> addAncestors(cell, affected));
        return Set.copyOf(affected);
    }

    private void addAncestors(Cell cell, Set<Cell> affected) {
        affected.addAll(cell.ancestorsIncludingSelf(viewableRange().minimum()));
    }

    @Override
    public List<LandFeature> providedFeatures() {
        return List.of(landFeature);
    }

    public LandFeature landFeature() {
        return landFeature;
    }
}
