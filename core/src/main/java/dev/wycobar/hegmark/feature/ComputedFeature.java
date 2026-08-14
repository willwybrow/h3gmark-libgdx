package dev.wycobar.hegmark.feature;


import java.util.Optional;

public abstract class ComputedFeature<T> implements Feature<T> {

    protected ComputedFeature() {
    }

    @Override
    final public Optional<ResolutionRange> settableRange() {
        return Optional.empty();
    }
}
