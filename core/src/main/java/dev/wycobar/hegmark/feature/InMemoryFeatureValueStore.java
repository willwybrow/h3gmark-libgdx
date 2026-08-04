package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import dev.wycobar.hegmark.planet.Cell;

public final class InMemoryFeatureValueStore implements FeatureValueStore {
    private final Map<Key, Object> values = new HashMap<>();

    @Override
    public <T> Optional<T> value(StoredFeature<T> feature, CellId cell) {
        Object value = values.get(new Key(feature, cell));
        return value == null ? Optional.empty() : Optional.of(feature.valueType().cast(value));
    }

    @Override
    public <T> void put(StoredFeature<T> feature, CellId cell, T value) {
        feature.validateValue(value);
        values.put(new Key(feature, cell), feature.valueType().cast(value));
    }

    @Override
    public void remove(StoredFeature<?> feature, CellId cell) {
        values.remove(new Key(feature, cell));
    }

    @Override
    public <T> Map<CellId, T> values(StoredFeature<T> feature) {
        Map<CellId, T> featureValues = new HashMap<>();
        values.forEach((key, value) -> {
            if (key.feature() == feature) featureValues.put(key.cell(), feature.valueType().cast(value));
        });
        return Map.copyOf(featureValues);
    }

    @Override
    public List<ExplicitFeatureValue<?>> values(CellId cell) {
        List<ExplicitFeatureValue<?>> cellValues = new ArrayList<>();
        values.forEach((key, value) -> {
            if (key.cell().equals(cell)) cellValues.add(explicitValue(key.feature(), value));
        });
        return List.copyOf(cellValues);
    }

    @Override
    public <T> Map<CellId, T> descendantValues(StoredFeature<T> feature, Cell root) {
        Map<CellId, T> descendants = new HashMap<>();
        values.forEach((key, value) -> {
            if (key.feature() == feature && root.strictlyContains(key.cell())) {
                descendants.put(key.cell(), feature.valueType().cast(value));
            }
        });
        return Map.copyOf(descendants);
    }

    @Override
    public int size() {
        return values.size();
    }

    private record Key(StoredFeature<?> feature, CellId cell) {
    }

    private <T> ExplicitFeatureValue<T> explicitValue(StoredFeature<T> feature, Object value) {
        return new ExplicitFeatureValue<>(feature, feature.valueType().cast(value));
    }
}
