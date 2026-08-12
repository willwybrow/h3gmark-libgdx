package dev.wycobar.hegmark.render;

public final class ElevationFeatureRenderer implements FeatureRenderer<Double> {

    public ElevationFeatureRenderer() {
    }

    @Override
    public RgbColor color(Double elevationMeters) {

        float rgb = (float) (5e-13 * (Math.pow(elevationMeters, 3)) + 0.5f);
        return new RgbColor(rgb, rgb, rgb);
    }

}
