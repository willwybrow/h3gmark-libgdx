package dev.wycobar.hegmark.render;

public final class LandFeatureRenderer implements FeatureRenderer<Boolean> {
    private static final RgbColor LAND = new RgbColor(0.20f, 0.62f, 0.24f);
    private static final RgbColor SEA = new RgbColor(0.08f, 0.32f, 0.72f);

    @Override
    public RgbColor color(Boolean land) {
        return land ? LAND : SEA;
    }
}
