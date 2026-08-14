package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.elevation.LandFeature;
import dev.wycobar.hegmark.planet.Cell;

public final class LandFeatureRenderer implements FeatureRenderer {
    private static final RgbColor LAND = new RgbColor(0.20f, 0.62f, 0.24f);
    private static final RgbColor SEA = new RgbColor(0.08f, 0.32f, 0.72f);

    private final LandFeature landFeature;

    public LandFeatureRenderer(LandFeature landFeature) {
        this.landFeature = landFeature;
    }

    @Override
    public RgbColor color(Cell cell) {
        return landFeature.valueAt(cell) ? LAND : SEA;
    }
}
