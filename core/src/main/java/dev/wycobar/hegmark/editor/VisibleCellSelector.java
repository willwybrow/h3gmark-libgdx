package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;

import java.util.List;

public final class VisibleCellSelector {
    private static final int MAX_LOCAL_CELLS = 1_800;

    private final PlanetGrid grid;

    public VisibleCellSelector(PlanetGrid grid) {
        this.grid = grid;
    }

    public List<CellId> select(CellId focus, int resolution) {
        if (resolution <= 2) return grid.cellsAtResolution(resolution);
        int radius = 23;
        List<CellId> cells = grid.disk(focus, radius);
        return cells.size() <= MAX_LOCAL_CELLS ? cells : List.copyOf(cells.subList(0, MAX_LOCAL_CELLS));
    }
}
