package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;

import java.util.OptionalDouble;

public interface FeatureValueStore {
    OptionalDouble value(String featureId, CellId cell);

    void put(String featureId, CellId cell, double value);

    void remove(String featureId, CellId cell);

    int size();
}
