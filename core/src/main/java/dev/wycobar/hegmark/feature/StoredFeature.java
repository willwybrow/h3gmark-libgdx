package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class StoredFeature<T> implements Feature<T> {

    protected final FeatureValueStore featureValueStore;

    protected StoredFeature(FeatureValueStore featureValueStore) {
        this.featureValueStore = featureValueStore;
    }

    public abstract T setAt(Cell cell, T value);

    public final Set<Feature<?>> provides() {
        return Stream.concat(Stream.of(this), this.additionalFeatures().stream()).collect(Collectors.toUnmodifiableSet());
    }

    protected abstract Set<ComputedFeature<?>> additionalFeatures();
}
