package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryFeatureDisplayCache {
    private final Map<Key, Object> values = new HashMap<>();

    public <T> Optional<T> value(Feature<T> feature, Cell cell, long context) {
        Object value = values.get(new Key(feature, cell.id(), context));
        return value == null ? Optional.empty() : Optional.of(feature.valueType().cast(value));
    }

    public <T> void put(Feature<T> feature, Cell cell, long context, T value) {
        values.put(new Key(feature, cell.id(), context), feature.valueType().cast(value));
    }

    public void remove(Feature<?> feature, Cell cell) {
        values.keySet().removeIf(key -> key.feature() == feature && key.cell().equals(cell.id()));
    }

    public void removeSubtree(Feature<?> feature, Cell root) {
        values.keySet().removeIf(key -> key.feature() == feature && root.contains(key.cell()));
    }

    public void clear() {
        values.clear();
    }

    public int size() {
        return values.size();
    }

    private record Key(Feature<?> feature, CellId cell, long context) {
    }
}
