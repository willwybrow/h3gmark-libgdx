package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetModel;

import java.util.Optional;
import java.util.OptionalDouble;

public final class ElevationResolver {
    public static final FeatureDefinition DEFINITION = new FeatureDefinition("elevation", "Elevation", 2, 7);

    private final PlanetGrid grid;
    private final FeatureValueStore store;
    private final ElevationGenerator generator;

    public ElevationResolver(PlanetGrid grid, FeatureValueStore store, ElevationGenerator generator) {
        this.grid = grid;
        this.store = store;
        this.generator = generator;
    }

    public ResolvedElevation resolve(CellId requestedCell, PlanetModel planet) {
        int requestedResolution = grid.resolution(requestedCell);
        if (requestedResolution < DEFINITION.minimumResolution()) return ResolvedElevation.notApplicable();

        CellId applicableCell = requestedResolution > DEFINITION.maximumResolution()
            ? grid.parent(requestedCell, DEFINITION.maximumResolution())
            : requestedCell;

        CellId candidate = applicableCell;
        for (int resolution = grid.resolution(candidate); resolution >= DEFINITION.minimumResolution(); resolution--) {
            OptionalDouble explicit = store.value(DEFINITION.id(), candidate);
            if (explicit.isPresent()) {
                return new ResolvedElevation(
                    true,
                    DEFINITION.appliesAt(requestedResolution),
                    explicit.getAsDouble(),
                    ElevationSource.EXPLICIT,
                    Optional.of(candidate)
                );
            }
            if (resolution > DEFINITION.minimumResolution()) candidate = grid.parent(candidate, resolution - 1);
        }

        long stableSeed = grid.stableSeed(
            applicableCell,
            planet.worldSeed(),
            DEFINITION.id(),
            ElevationGenerator.ALGORITHM_VERSION
        );
        double generated = generator.generate(planet, stableSeed, grid.center(applicableCell));
        return new ResolvedElevation(
            true,
            DEFINITION.appliesAt(requestedResolution),
            generated,
            ElevationSource.GENERATED,
            Optional.of(applicableCell)
        );
    }
}
