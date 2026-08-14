package dev.wycobar.hegmark.feature.elevation;

import dev.wycobar.hegmark.feature.ComputedFeature;
import dev.wycobar.hegmark.feature.ResolutionRange;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.Layer;

import java.util.Optional;

public final class LandFeature extends ComputedFeature<Boolean> {

    private static final ResolutionRange VIEWABLE = Layer.anywhere();
    private final ElevationFeature elevationFeature;

    public LandFeature(ElevationFeature computedFrom) {
        super(computedFrom);
        this.elevationFeature = computedFrom;
    }

    @Override
    public String id() {
        return "land";
    }

    @Override
    public String name() {
        return "Land";
    }

    @Override
    public ResolutionRange viewableRange() {
        return VIEWABLE;
    }

    @Override
    public Optional<ResolutionRange> settableRange() {
        return Optional.empty();
    }

    @Override
    public Boolean valueAt(Cell cell) {
        return this.elevationFeature.valueAt(cell) > cell.planet().definition().seaLevelMeters();
    }
}
