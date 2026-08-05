package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class InMemoryFeatureValueStore implements FeatureValueStore {
    private final Map<Key, Double> doubleMap = new HashMap<>();

    @Override
    public Double put(StoredFeature<Double> feature, CellId cell, Double value) {
        return doubleMap.put(key(feature, cell), value);
    }

    @Override
    public Double getDouble(StoredFeature<Double> feature, CellId cell) {
        return doubleMap.get(key(feature, cell));
    }

    @Override
    public boolean remove(StoredFeature<?> feature, CellId cell) {
        return doubleMap.remove(key(feature, cell)) != null;
    }

    @Override
    public Set<CellId> doubleValueCellIds(StoredFeature<Double> feature) {
        return doubleMap.keySet().stream()
            .filter(key -> key.feature() == feature)
            .map(Key::cell)
            .collect(Collectors.toUnmodifiableSet());
    }

    private Key key(StoredFeature<?> feature, CellId cell) {
        return new Key(feature, cell);
    }
}
