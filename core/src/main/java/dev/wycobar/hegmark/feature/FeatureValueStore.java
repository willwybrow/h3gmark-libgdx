package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import dev.wycobar.hegmark.planet.Cell;

public interface FeatureValueStore {
    <T> Optional<T> value(StoredFeature<T> feature, CellId cell);

    <T> void put(StoredFeature<T> feature, CellId cell, T value);

    void remove(StoredFeature<?> feature, CellId cell);

    <T> Map<CellId, T> values(StoredFeature<T> feature);

    List<ExplicitFeatureValue<?>> values(CellId cell);

    <T> Map<CellId, T> descendantValues(StoredFeature<T> feature, Cell root);

    int size();
}
