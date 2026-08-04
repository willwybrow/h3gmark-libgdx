package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;

public final class SelectionProjector {
    private final PlanetGrid grid;

    public SelectionProjector(PlanetGrid grid) {
        this.grid = grid;
    }

    public CellId atResolution(CellId selectedCell, int displayResolution) {
        return grid.cellAt(grid.center(selectedCell), displayResolution);
    }
}
