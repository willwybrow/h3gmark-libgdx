package dev.wycobar.hegmark.render;

public final class ElevationFeatureRenderer implements FeatureRenderer<Double> {

    private static final double UPPER_BOUND = 8000d;
    private static final double LOWER_BOUND = - UPPER_BOUND;

    public ElevationFeatureRenderer() {
    }

    @Override
    public RgbColor color(Double elevationMeters) {

        double bounded = Math.clamp(elevationMeters, LOWER_BOUND, UPPER_BOUND);

        float rgb = (float) ((Math.pow(bounded, 3) / (2d * Math.pow(UPPER_BOUND, 3))) + 0.5d);
        return new RgbColor(rgb, rgb, rgb);
    }

}
