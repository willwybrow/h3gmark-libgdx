package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.Set;

public interface FeatureValueStore {
    Double put(StoredFeature<Double> feature, CellId cell, Double value);

    Double getDouble(StoredFeature<Double> feature, CellId cell);

    boolean remove(StoredFeature<?> feature, CellId cell);

    Set<CellId> doubleValueCellIds(StoredFeature<Double> feature);

    record Key(StoredFeature<?> feature, CellId cell) {
    }
}
