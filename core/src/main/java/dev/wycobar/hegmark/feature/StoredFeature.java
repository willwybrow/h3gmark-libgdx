package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

public abstract class StoredFeature<T> implements Feature<T> {

    protected final FeatureValueStore featureValueStore;

    protected StoredFeature(FeatureValueStore featureValueStore) {
        this.featureValueStore = featureValueStore;
    }

    public abstract FeatureMutationResult fillGapsAt(Cell cell, T value);

    public abstract FeatureMutationResult overwriteAt(Cell cell, T value, boolean destructiveEditConfirmed);

    public abstract FeatureMutationResult eraseAt(Cell cell);

}
