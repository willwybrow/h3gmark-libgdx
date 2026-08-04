package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {
    @Test
    void exposesIdentityGeometryAndTopologyAsCells() {
        var fixture = TestPlanetFactory.create();
        Cell cell = fixture.planet().cellAt(new PlanetLatLon(10.0, 20.0), 4);

        assertEquals(4, cell.resolution());
        assertEquals(cell, fixture.planet().cell(cell.id()));
        assertEquals(cell.parent().orElseThrow(), cell.parent(3));
        assertTrue(cell.parent().orElseThrow().children().contains(cell));
        assertTrue(cell.neighbors().stream().allMatch(neighbor -> neighbor.planet() == fixture.planet()));
        assertEquals(cell.id(), fixture.planet().cellAt(cell.center(), 4).id());
        assertEquals(cell.center(), cell.geometry().center());
    }

    @Test
    void leavesHaveNoChildrenAndRootHasNoParent() {
        var fixture = TestPlanetFactory.create();
        Cell leaf = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 15);
        Cell root = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 0);
        assertTrue(leaf.children().isEmpty());
        assertTrue(root.parent().isEmpty());
    }

    @Test
    void cellIdentityIncludesPlanetAggregate() {
        var first = TestPlanetFactory.create();
        var second = TestPlanetFactory.create();
        Cell firstCell = first.planet().cellAt(new PlanetLatLon(0.0, 0.0), 3);
        Cell secondCell = second.planet().cell(firstCell.id());
        assertNotEquals(firstCell, secondCell);
    }
}
