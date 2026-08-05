package dev.wycobar.hegmark.editor;

public final class CameraStepPolicy {
    private static final double DEGREES_PER_SURFACE_GAP = 5.0;
    private static final double MINIMUM_DEGREES = 0.01;
    private static final double MAXIMUM_DEGREES = 10.0;

    public float longitudeDegrees(double distanceToSurface) {
        if (!Double.isFinite(distanceToSurface) || distanceToSurface < 0.0) {
            throw new IllegalArgumentException("Distance to surface must be finite and non-negative");
        }
        return (float) Math.clamp(
            distanceToSurface * DEGREES_PER_SURFACE_GAP,
            MINIMUM_DEGREES,
            MAXIMUM_DEGREES
        );
    }
}
