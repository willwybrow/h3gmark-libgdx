package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Cell;

public final class LandFeature implements ComputedFeature<Boolean> {
    private final ElevationFeature elevation;
    private final ResolutionRange viewableRange;

    LandFeature(ElevationFeature elevation, ResolutionRange viewableRange) {
        this.elevation = elevation;
        this.viewableRange = viewableRange;
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
    public Class<Boolean> valueType() {
        return Boolean.class;
    }

    @Override
    public ResolutionRange viewableRange() {
        return viewableRange;
    }

    @Override
    public Boolean compute(Cell cell) {
        return cell.feature(elevation) > cell.planet().definition().seaLevelMeters();
    }
}
