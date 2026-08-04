package dev.wycobar.hegmark.feature.elevation;

import dev.wycobar.hegmark.feature.ComputedFeature;
import dev.wycobar.hegmark.feature.FeatureValueStore;
import dev.wycobar.hegmark.feature.ResolutionRange;
import dev.wycobar.hegmark.feature.StoredFeature;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.Layer;


import java.util.Optional;
import java.util.Set;

public final class ElevationFeature extends StoredFeature<Double> {
    private static final ResolutionRange VIEWABLE = Layer.anywhere();
    private static final ResolutionRange SETTABLE = Layer.anywhere();

    private final LandFeature landFeature;

    private final ElevationGenerator generator = new ElevationGenerator();

    public ElevationFeature(FeatureValueStore featureValueStore) {
        super(featureValueStore);
        landFeature = new LandFeature(this);
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
    public Optional<Double> valueAt(Cell cell) {
        return Optional.ofNullable(this.featureValueStore.getDouble(this, cell));
    }

    @Override
    public Double setAt(Cell cell, Double value) {
        this.featureValueStore.put(this, cell, value);
        return value;
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
    protected Set<ComputedFeature<?>> additionalFeatures() {
        return Set.of(landFeature);
    }
}
