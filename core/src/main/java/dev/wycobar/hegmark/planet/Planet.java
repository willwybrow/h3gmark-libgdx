package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.feature.FeatureChangeBus;
import dev.wycobar.hegmark.feature.FeatureRegistry;
import dev.wycobar.hegmark.feature.FeatureValueStore;

public final class Planet {
    private final PlanetModel definition;
    private final PlanetGrid grid;
    private final FeatureRegistry features;
    private final FeatureValueStore explicitValues;
    private final FeatureChangeBus changes;

    public Planet(
        PlanetModel definition,
        PlanetGrid grid,
        FeatureRegistry features,
        FeatureValueStore explicitValues,
        FeatureChangeBus changes
    ) {
        this.definition = definition;
        this.grid = grid;
        this.features = features;
        this.explicitValues = explicitValues;
        this.changes = changes;
    }

    public PlanetModel definition() {
        return definition;
    }

    public Cell cell(CellId id) {
        return new Cell(this, id);
    }

    public Cell cellAt(PlanetLatLon coordinate, int resolution) {
        return cell(grid.cellAt(coordinate, resolution));
    }

    PlanetGrid grid() {
        return grid;
    }

}
