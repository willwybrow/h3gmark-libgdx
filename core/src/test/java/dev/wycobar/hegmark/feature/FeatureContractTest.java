package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.Planet;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeatureContractTest {
    @Test
    void distinguishesViewableSettableAndComputedFeatures() {
        ElevationFeature elevation = ElevationFeature.INSTANCE;
        assertTrue(elevation.isViewableAt(1));
        assertFalse(elevation.isSettableAt(1));
        assertTrue(elevation.isSettableAt(7));
        assertTrue(elevation.isViewableAt(8));
        assertFalse(elevation.isSettableAt(8));
        assertTrue(elevation.landFeature().settableRange().isEmpty());

        FeatureRegistry registry = new FeatureRegistry(List.of(elevation));
        assertEquals(List.of("elevation", "land"), registry.features().stream().map(Feature::id).toList());
    }

    @Test
    void computedFeatureUsesTypedCellRelationship() {
        var fixture = TestPlanetFactory.create();
        Cell cell = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 4);
        fixture.planet().fillGaps(fixture.elevation(), List.of(cell), -1.0);
        assertFalse(cell.feature(fixture.elevation().landFeature()));
    }

    @Test
    void rejectsCyclicCellFeatureComputation() {
        CyclicFeature first = new CyclicFeature("first");
        CyclicFeature second = new CyclicFeature("second");
        first.dependency = second;
        second.dependency = first;
        ElevationFeature elevation = ElevationFeature.INSTANCE;
        FeatureRegistry registry = new FeatureRegistry(List.of(elevation, first, second));
        Planet planet = new Planet(
            new PlanetModel("Test", 1.0, 1.0, 0.0, 1L),
            new H3PlanetGrid(),
            registry,
            new InMemoryFeatureValueStore(),
            new InMemoryFeatureDisplayCache(),
            new FeatureChangeBus()
        );
        Cell cell = planet.cellAt(new PlanetLatLon(0.0, 0.0), 3);
        assertThrows(IllegalStateException.class, () -> cell.feature(first));
    }

    private static final class CyclicFeature implements ComputedFeature<Boolean> {
        private final String id;
        private Feature<Boolean> dependency;

        private CyclicFeature(String id) {
            this.id = id;
        }

        @Override
        public Boolean compute(Cell cell) {
            return cell.feature(dependency);
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String name() {
            return id;
        }

        @Override
        public Class<Boolean> valueType() {
            return Boolean.class;
        }

        @Override
        public ResolutionRange viewableRange() {
            return new ResolutionRange(0, 15);
        }
    }
}
