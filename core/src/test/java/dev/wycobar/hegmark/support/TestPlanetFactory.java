package dev.wycobar.hegmark.support;

import dev.wycobar.hegmark.feature.ElevationFeature;
import dev.wycobar.hegmark.feature.FeatureChangeBus;
import dev.wycobar.hegmark.feature.FeatureRegistry;
import dev.wycobar.hegmark.feature.InMemoryFeatureDisplayCache;
import dev.wycobar.hegmark.feature.InMemoryFeatureValueStore;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.Planet;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetModel;

import java.util.List;

public final class TestPlanetFactory {
    private TestPlanetFactory() {
    }

    public static Fixture create() {
        return create(99L);
    }

    public static Fixture create(long seed) {
        PlanetGrid grid = new H3PlanetGrid();
        ElevationFeature elevation = ElevationFeature.INSTANCE;
        FeatureRegistry features = new FeatureRegistry(List.of(elevation));
        InMemoryFeatureValueStore values = new InMemoryFeatureValueStore();
        InMemoryFeatureDisplayCache cache = new InMemoryFeatureDisplayCache();
        FeatureChangeBus changes = new FeatureChangeBus();
        PlanetModel definition = new PlanetModel("Test", 6_000_000.0, 5_900_000.0, 0.0, seed);
        Planet planet = new Planet(definition, grid, features, values, cache, changes);
        return new Fixture(planet, grid, elevation, features, values, cache, changes);
    }

    public record Fixture(
        Planet planet,
        PlanetGrid grid,
        ElevationFeature elevation,
        FeatureRegistry features,
        InMemoryFeatureValueStore values,
        InMemoryFeatureDisplayCache cache,
        FeatureChangeBus changes
    ) {
    }
}
