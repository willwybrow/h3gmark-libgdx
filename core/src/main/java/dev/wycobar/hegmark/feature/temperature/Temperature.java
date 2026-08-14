package dev.wycobar.hegmark.feature.temperature;

import dev.wycobar.hegmark.feature.ComputedFeature;
import dev.wycobar.hegmark.feature.ResolutionRange;
import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.feature.elevation.LandFeature;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.Layer;

public abstract class Temperature extends ComputedFeature<Double> {
    protected final ElevationFeature elevationFeature;
    protected final LandFeature landFeature;

    private static final double RESTING_SEA_TEMPERATURE = 2.0d;
    private static final double SHALLOW_DEPTH_RATE_OF_TEMPERATURE_DECREASE = 0.5d / 100.0d;
    private static final double THERMOCLINE_RATE_OF_TEMPERATURE_DECREASE = 4.0d / 100.0d;
    private static final double DEEP_RATE_OF_TEMPERATURE_DECREASE = 0.5d / 100.0d;

    private static final double THERMOCLINE_START_DEPTH = 200d;
    private static final double THERMOCLINE_END_DEPTH = 1500d;
    private static final double THERMOCLINE_MIDPOINT = THERMOCLINE_START_DEPTH + (THERMOCLINE_END_DEPTH - THERMOCLINE_START_DEPTH) / 2.0d;
    private static final double THERMOCLINE_MIDPOINT_SQUARED = Math.pow(THERMOCLINE_MIDPOINT, 2);

    protected Temperature(ElevationFeature elevationFeature, LandFeature landFeature) {
        this.elevationFeature = elevationFeature;
        this.landFeature = landFeature;
    }

    @Override
    public ResolutionRange viewableRange() {
        return Layer.anywhere();
    }

    protected double getLatitudeAdjustedForAxialTilt(Cell cell) {
        return cell.center().latitudeDegrees() + cell.planet().definition().axialTiltDegrees();
    }

    protected double seaSurfaceTemperature(Cell cell) {
        /*
        (c) Wycobar
            abslat = abs(lat)
            if abslat > 90:  # start going down the other side, and our cos function isn't right for that (which may mean it isnt right full stop)
                abslat = 90 - (abslat-90)
            temp = (math.cos(math.radians(abslat))-0.2079116908)*35.09709672
            return temp
         */
        double adjustedLatitude = getLatitudeAdjustedForAxialTilt(cell);

        double absoluteLatitude = Math.abs(adjustedLatitude);

        while (absoluteLatitude > 90) {
            absoluteLatitude = 90 - (absoluteLatitude - 90);
        }

        return (Math.cos(Math.toRadians(absoluteLatitude))-0.2079116908d) * 35.09709672d;
    }

    protected double underwaterTemperature(Cell cell) {
        /*
        def underwater_temperature(sea_surface_temperature, depth_in_metres):
            # the actual ocean temperature depends on water salinity
            # but for earth's oceans it's generally 1-3 degrees C
            # we're going to go with 2 to split the difference
            resting_sea_temp = 2

            # the first 200 or so meters are essentially at sea surface temperature thanks to the sun
            # after that we enter the "thermocline", a rapidly cooling zone from about 200 to 1000 m
            # and finally a gradual approach towards the limit

            early_rate_of_change = 0.5 / float(100) # early part the roc is 0.5 degrees per 100m
            thermocline_rate_of_change = 4.0 / float(100)  # thermocline the highest roc is about 4 degrees per 100m
            deep_rate_of_change = 0.5 / float(100)

            current_temperature = sea_surface_temperature
            depth = 0
            while depth < depth_in_metres:
                depth += 100
                if depth_in_metres <= 100:
                    rate_of_change_per_hundred_metres = early_rate_of_change
                elif depth_in_metres <= 1500:
                    midpoint = 100 + ((1500 - 100) / 2.0)
                    rate_of_change_per_hundred_metres = thermocline_rate_of_change * ((pow(midpoint, 2) - pow(midpoint - depth_in_metres, 2)) / pow(midpoint, 2))
                else:
                    rate_of_change_per_hundred_metres = deep_rate_of_change
                current_temperature = resting_sea_temp + ((current_temperature - resting_sea_temp) * rate_of_change_per_hundred_metres)

            # https://en.wikipedia.org/wiki/Thermocline

            return current_temperature
         */

        double rateOfChange = 0.0d;

        double lowestPoint = elevationFeature.valueAt(cell);

        double currentTemperature = seaSurfaceTemperature(cell);
        double currentHeight = 0;

        while (currentHeight > lowestPoint) {
            currentHeight -= 100d;
            if (currentHeight <= THERMOCLINE_START_DEPTH) {
                rateOfChange = SHALLOW_DEPTH_RATE_OF_TEMPERATURE_DECREASE;
            } else if (currentHeight <= THERMOCLINE_END_DEPTH) {
                rateOfChange = THERMOCLINE_RATE_OF_TEMPERATURE_DECREASE * ((THERMOCLINE_MIDPOINT_SQUARED - Math.pow(THERMOCLINE_MIDPOINT + currentHeight, 2)) / THERMOCLINE_MIDPOINT_SQUARED);
            } else {
                rateOfChange = DEEP_RATE_OF_TEMPERATURE_DECREASE;
            }

            currentTemperature = RESTING_SEA_TEMPERATURE - ((currentTemperature - RESTING_SEA_TEMPERATURE) * rateOfChange);
        }

        return currentTemperature;
    }
}
