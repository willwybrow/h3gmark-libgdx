package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;

public final class ElevationEditor {
    private final PlanetGrid grid;
    private final FeatureValueStore store;

    public ElevationEditor(PlanetGrid grid, FeatureValueStore store) {
        this.grid = grid;
        this.store = store;
    }

    public void paint(CellId cell, double meters) {
        requireEditable(cell);
        store.put(ElevationResolver.DEFINITION.id(), cell, meters);
    }

    public void erase(CellId cell) {
        requireEditable(cell);
        store.remove(ElevationResolver.DEFINITION.id(), cell);
    }

    private void requireEditable(CellId cell) {
        int resolution = grid.resolution(cell);
        if (!ElevationResolver.DEFINITION.appliesAt(resolution)) {
            throw new IllegalArgumentException("Elevation is not editable at resolution " + resolution);
        }
    }
}
