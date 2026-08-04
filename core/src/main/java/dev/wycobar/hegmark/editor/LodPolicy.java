package dev.wycobar.hegmark.editor;

public final class LodPolicy {
    public int resolution(double distanceToSurface) {
        return distanceToSurface > 0.5 ? 2
            : distanceToSurface > 0.19 ? 3
            : distanceToSurface > 0.072 ? 4
            : distanceToSurface > 0.027 ? 5
            : distanceToSurface > 0.010 ? 6
            : distanceToSurface > 0.0038 ? 7
            : 8;
    }
}
