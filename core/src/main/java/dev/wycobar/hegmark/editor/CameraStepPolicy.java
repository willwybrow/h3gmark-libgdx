package dev.wycobar.hegmark.editor;

public final class CameraStepPolicy {
    public float longitudeDegrees(int resolution) {
        if (resolution < 0 || resolution > 15) throw new IllegalArgumentException("Grid resolution must be 0 through 15");
        double refinement = Math.pow(Math.sqrt(7.0), Math.max(0, resolution - 2));
        return (float) Math.max(0.01, 30.0 / refinement);
    }
}
