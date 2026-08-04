package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import dev.wycobar.hegmark.planet.Cell;

public final class InMemoryFeatureValueStore implements FeatureValueStore {
    private final Map<Key, Double> doubleMap = new HashMap<>();

    @Override
    public Double put(StoredFeature<Double> feature, Cell cell, Double value) {
        return doubleMap.put(key(feature, cell), value);
    }

    @Override
    public Double getDouble(StoredFeature<Double> feature, Cell cell) {
        return doubleMap.get(key(feature, cell));
    }

    private Key key(StoredFeature<?> feature, Cell cell) {
        return new Key(feature, cell.id());
    }
}
