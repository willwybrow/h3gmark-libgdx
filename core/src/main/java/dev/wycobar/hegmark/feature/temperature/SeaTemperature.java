package dev.wycobar.hegmark.feature.temperature;

import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.feature.elevation.LandFeature;
import dev.wycobar.hegmark.planet.Cell;

public final class SeaTemperature extends Temperature {
    public SeaTemperature(ElevationFeature elevationFeature, LandFeature landFeature) {
        super(elevationFeature, landFeature);
    }

    @Override
    public String id() {
        return "sea_temperature";
    }

    @Override
    public String name() {
        return "Sea temperature (C)";
    }

    @Override
    public Double valueAt(Cell cell) {
        return seaSurfaceTemperature(cell);
    }
}
