package dev.wycobar.hegmark.feature;


public abstract class ComputedFeature<T> implements Feature<T> {
    protected final StoredFeature<?> computedFrom;

    protected ComputedFeature(StoredFeature<?> computedFrom) {
        this.computedFrom = computedFrom;
    }
}
