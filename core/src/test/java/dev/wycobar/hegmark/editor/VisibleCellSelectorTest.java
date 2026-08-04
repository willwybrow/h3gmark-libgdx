package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisibleCellSelectorTest {
    private final PlanetGrid grid = new H3PlanetGrid();
    private final VisibleCellSelector selector = new VisibleCellSelector(grid);

    @Test
    void usesCompleteLowResolutionPlanet() {
        CellId focus = grid.cellAt(new PlanetLatLon(0.0, 0.0), 2);
        assertEquals(5_882, selector.select(focus, 2).size());
    }

    @Test
    void boundsFineResolutionWorkToLocalDisk() {
        CellId focus = grid.cellAt(new PlanetLatLon(0.0, 0.0), 8);
        List<CellId> cells = selector.select(focus, 8);
        assertTrue(cells.contains(focus));
        assertTrue(cells.size() <= 1_800);
        assertTrue(cells.size() > 1_000);
    }
}
