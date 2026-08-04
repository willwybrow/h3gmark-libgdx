package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.feature.FeatureValueSource;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellFeatureResolutionTest {
    @Test
    void resolvesGeneratedDirectAndNearestAncestorValues() {
        var fixture = TestPlanetFactory.create();
        Cell cell = fixture.planet().cellAt(new PlanetLatLon(20.0, 30.0), 6);
        assertEquals(FeatureValueSource.GENERATED, cell.resolvedFeature(fixture.elevation()).effectiveSource());

        Cell coarse = cell.parent(3);
        Cell nearer = cell.parent(5);
        fixture.planet().fillGaps(fixture.elevation(), java.util.List.of(coarse), 300.0);
        fixture.planet().fillGaps(fixture.elevation(), java.util.List.of(nearer), 900.0);
        var resolved = cell.resolvedFeature(fixture.elevation());
        assertEquals(900.0, resolved.effectiveValue());
        assertEquals(nearer, resolved.sourceCell().orElseThrow());
    }

    @Test
    void parentDisplayIsFeatureOwnedAggregationOfChildCells() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(10.0, 20.0), 3);
        fixture.planet().fillGaps(fixture.elevation(), java.util.List.of(parent), 500.0);
        parent.children().forEach(child -> fixture.planet().fillGaps(fixture.elevation(), java.util.List.of(child), -1_000.0));

        var resolved = parent.resolvedFeature(fixture.elevation());
        assertEquals(500.0, resolved.effectiveValue());
        assertEquals(-1_000.0, resolved.displayValue(), 1e-9);
        assertEquals(FeatureValueSource.AGGREGATED, resolved.displaySource());
        assertFalse(parent.feature(fixture.elevation().landFeature()));
    }

    @Test
    void cacheLossReconstructsMultiLevelAggregationFromCellBehavior() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(10.0, 20.0), 3);
        Cell child = parent.children().getFirst();
        child.children().forEach(grandchild -> fixture.planet().fillGaps(fixture.elevation(), java.util.List.of(grandchild), -500.0));
        double expected = parent.feature(fixture.elevation());

        fixture.cache().clear();
        assertEquals(expected, parent.feature(fixture.elevation()), 1e-9);
    }

    @Test
    void viewOnlyFineCellInheritsMaximumSettableAncestor() {
        var fixture = TestPlanetFactory.create();
        Cell fine = fixture.planet().cellAt(new PlanetLatLon(20.0, 30.0), 8);
        Cell parent = fine.parent(7);
        fixture.planet().fillGaps(fixture.elevation(), java.util.List.of(parent), 750.0);
        var resolved = fine.resolvedFeature(fixture.elevation());
        assertFalse(resolved.directlyEditable());
        assertEquals(750.0, resolved.displayValue());
    }

    @Test
    void deterministicDefaultsDependOnPlanetSeed() {
        var first = TestPlanetFactory.create(99L);
        var second = TestPlanetFactory.create(100L);
        Cell firstCell = first.planet().cellAt(new PlanetLatLon(20.0, 30.0), 5);
        Cell secondCell = second.planet().cell(firstCell.id());
        assertNotEquals(firstCell.feature(first.elevation()), secondCell.feature(second.elevation()));
    }
}
