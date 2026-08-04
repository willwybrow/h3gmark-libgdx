package dev.wycobar.hegmark.planet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class H3PlanetGridTest {
    private static H3PlanetGrid grid;

    @BeforeAll
    static void createGrid() {
        grid = new H3PlanetGrid();
    }

    @Test
    void mapsCoordinatesAndHierarchyWithoutLeakingH3Types() {
        CellId child = grid.cellAt(new PlanetLatLon(51.5074, -0.1278), 6);
        CellId parent = grid.parent(child, 4);

        assertEquals(6, grid.resolution(child));
        assertEquals(4, grid.resolution(parent));
        assertTrue(grid.children(parent, 6).contains(child));
        assertEquals(child, grid.cellAt(grid.center(child), 6));
    }

    @Test
    void preservesActualBoundaryAndNeighborCountsForPentagons() {
        CellId pentagon = grid.cellsAtResolution(0).stream().filter(grid::isPentagon).findFirst().orElseThrow();
        CellGeometry geometry = grid.geometry(pentagon);

        assertTrue(grid.isPentagon(pentagon));
        assertNotEquals(6, geometry.boundary().size());
        assertNotEquals(6, grid.neighbors(pentagon).size());
    }

    @Test
    void diskContainsItsCenterAndGlobalResolutionIsComplete() {
        CellId center = grid.cellAt(new PlanetLatLon(0.0, 0.0), 3);
        assertTrue(grid.disk(center, 2).contains(center));
        assertEquals(5_882, grid.cellsAtResolution(2).size());
    }

    @Test
    void cellGeometryIsImmutable() {
        CellGeometry geometry = grid.geometry(grid.cellAt(new PlanetLatLon(0.0, 0.0), 2));
        List<PlanetLatLon> boundary = geometry.boundary();
        assertThrows(UnsupportedOperationException.class, () -> boundary.add(new PlanetLatLon(0.0, 0.0)));
    }

    @Test
    void preventsAccidentalFineWholePlanetMaterialization() {
        assertThrows(IllegalArgumentException.class, () -> grid.cellsAtResolution(3));
    }

    @Test
    void stableSeedsIncludeWorldFeatureAndAlgorithmIdentity() {
        CellId cell = grid.cellAt(new PlanetLatLon(0.0, 0.0), 4);
        long seed = grid.stableSeed(cell, 1L, "elevation", 1);
        assertEquals(seed, grid.stableSeed(cell, 1L, "elevation", 1));
        assertNotEquals(seed, grid.stableSeed(cell, 2L, "elevation", 1));
        assertNotEquals(seed, grid.stableSeed(cell, 1L, "temperature", 1));
        assertNotEquals(seed, grid.stableSeed(cell, 1L, "elevation", 2));
    }
}
