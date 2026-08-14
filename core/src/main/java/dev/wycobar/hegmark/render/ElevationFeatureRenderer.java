package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.planet.Cell;

public final class ElevationFeatureRenderer implements FeatureRenderer {

    private static final double UPPER_BOUND = 8000d;
    private static final double LOWER_BOUND = - UPPER_BOUND;

    private final ElevationFeature elevationFeature;

    public ElevationFeatureRenderer(ElevationFeature elevationFeature) {
        this.elevationFeature = elevationFeature;
    }

    @Override
    public RgbColor color(Cell cell) {
        double elevationMeters = elevationFeature.valueAt(cell);
        double bounded = Math.clamp(elevationMeters, LOWER_BOUND, UPPER_BOUND);

        float rgb = (float) ((Math.pow(bounded, 3) / (2d * Math.pow(UPPER_BOUND, 3))) + 0.5d);
        return new RgbColor(rgb, rgb, rgb);
    }

}
