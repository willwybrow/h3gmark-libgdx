package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.temperature.OuterCrustTemperature;
import dev.wycobar.hegmark.planet.Cell;

public final class OuterCrustTemperatureFeatureRenderer implements FeatureRenderer {
    private static final double COLD_CELSIUS = -50.0;
    private static final double HOT_CELSIUS = 50.0;

    private final OuterCrustTemperature outerCrustTemperature;

    public OuterCrustTemperatureFeatureRenderer(OuterCrustTemperature outerCrustTemperature) {
        this.outerCrustTemperature = outerCrustTemperature;
    }

    @Override
    public RgbColor color(Cell cell) {
        double temperatureCelsius = outerCrustTemperature.valueAt(cell);
        float heat = (float) Math.clamp(
            (temperatureCelsius - COLD_CELSIUS) / (HOT_CELSIUS - COLD_CELSIUS),
            0.0,
            1.0
        );
        float temperate = 1.0f - Math.abs(heat * 2.0f - 1.0f);
        return new RgbColor(heat, 0.2f + temperate * 0.6f, 1.0f - heat);
    }
}
