package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import dev.wycobar.hegmark.planet.Cell;

public interface FeatureValueStore {
    Double put(StoredFeature<Double> feature, Cell cell, Double value);

    Double getDouble(StoredFeature<Double> feature, Cell cell);

    record Key(StoredFeature<?> feature, CellId cell) {
    }
}
