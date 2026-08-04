package dev.wycobar.hegmark.feature;

public final class ElevationStyle {
    public RgbColor color(double elevationMeters, double seaLevelMeters) {
        double relative = elevationMeters - seaLevelMeters;
        if (relative < 0.0) {
            float depth = clamp((float) (-relative / 3_000.0));
            return new RgbColor(0.05f, 0.28f + 0.16f * (1.0f - depth), 0.58f + 0.32f * (1.0f - depth));
        }
        float height = clamp((float) (relative / 3_000.0));
        return new RgbColor(0.12f + 0.28f * height, 0.58f - 0.22f * height, 0.16f + 0.08f * height);
    }

    private float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
