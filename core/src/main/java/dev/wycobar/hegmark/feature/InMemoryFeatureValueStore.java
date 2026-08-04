package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

public final class InMemoryFeatureValueStore implements FeatureValueStore {
    private final Map<Key, Double> values = new HashMap<>();

    @Override
    public OptionalDouble value(String featureId, CellId cell) {
        Double value = values.get(new Key(featureId, cell));
        return value == null ? OptionalDouble.empty() : OptionalDouble.of(value);
    }

    @Override
    public void put(String featureId, CellId cell, double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Feature values must be finite");
        values.put(new Key(featureId, cell), value);
    }

    @Override
    public void remove(String featureId, CellId cell) {
        values.remove(new Key(featureId, cell));
    }

    @Override
    public int size() {
        return values.size();
    }

    private record Key(String featureId, CellId cell) {
    }
}
