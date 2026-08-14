package dev.wycobar.hegmark.feature.temperature;

import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.feature.elevation.LandFeature;
import dev.wycobar.hegmark.planet.Cell;

public final class OuterCrustTemperature extends Temperature {
    /* This is the temperature at the outer edge of the crust; previously called "surface" temperature but the sea also has a surface and sea ice complicates stuff a bit
    So this is the temperature wherever the crust touches something else, whether that's air or water.

     */
    public OuterCrustTemperature(ElevationFeature elevationFeature, LandFeature landFeature) {
        super(elevationFeature, landFeature);
    }

    @Override
    public String id() {
        return "crust_temperature";
    }

    @Override
    public String name() {
        return "Temperature at crust (°C)";
    }

    @Override
    public Double valueAt(Cell cell) {
        /*
        # http://www.onthesnow.com/news/a/15157/does-elevation-affect-temperature-
        # return sea_surface_temperature - (9.8e-3*elevation_in_metres)
        # https://www.sciencedirect.com/science/article/pii/S111098231730011X
        return sea_surface_temperature + (-0.0035 * elevation_in_metres)
         */
        if (landFeature.valueAt(cell)) { // on land, we use elevation to decrease the sea surface temperature
            return seaSurfaceTemperature(cell)  + (-0.0035d * elevationFeature.valueAt(cell));
        } else { // underwater we have to use the underwater depth
            return underwaterTemperature(cell);
        }
    }
}
