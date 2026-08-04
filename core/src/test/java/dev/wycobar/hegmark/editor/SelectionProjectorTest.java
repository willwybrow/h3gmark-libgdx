package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectionProjectorTest {
    @Test
    void keepsSelectionVisibleAcrossDisplayResolutionChanges() {
        PlanetGrid grid = new H3PlanetGrid();
        SelectionProjector projector = new SelectionProjector(grid);
        CellId selected = grid.cellAt(new PlanetLatLon(12.0, 34.0), 7);

        CellId coarse = projector.atResolution(selected, 3);
        CellId fine = projector.atResolution(selected, 8);
        assertEquals(grid.parent(selected, 3), coarse);
        assertEquals(selected, grid.parent(fine, 7));
    }
}
