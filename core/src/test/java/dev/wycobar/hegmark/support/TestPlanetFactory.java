package dev.wycobar.hegmark.support;

import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.feature.FeatureChangeBus;
import dev.wycobar.hegmark.feature.FeatureRegistry;
import dev.wycobar.hegmark.feature.InMemoryFeatureValueStore;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.Planet;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetModel;

public final class TestPlanetFactory {
    private TestPlanetFactory() {
    }

    public static Fixture create() {
        return create(99L);
    }

    public static Fixture create(long seed) {
        PlanetGrid grid = new H3PlanetGrid();
        InMemoryFeatureValueStore values = new InMemoryFeatureValueStore();
        ElevationFeature elevation = new ElevationFeature(values);
        FeatureRegistry features = new FeatureRegistry();
        FeatureChangeBus changes = new FeatureChangeBus();
        PlanetModel definition = new PlanetModel("Test", 6_000_000.0, 5_900_000.0, 0.0, seed);
        Planet planet = new Planet(definition, grid, features, values, changes);
        return new Fixture(planet, grid, elevation, features, values, changes);
    }

    public record Fixture(
        Planet planet,
        PlanetGrid grid,
        ElevationFeature elevation,
        FeatureRegistry features,
        InMemoryFeatureValueStore values,
        FeatureChangeBus changes
    ) {
    }
}
