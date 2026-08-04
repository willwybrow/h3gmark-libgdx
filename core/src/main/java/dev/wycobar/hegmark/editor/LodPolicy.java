package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.Layer;

public final class LodPolicy {
    private static final double FIRST_DETAIL_DISTANCE = 0.5;
    private static final double H3_REFINEMENT = Math.sqrt(7.0);

    public int resolution(double distanceToSurface) {
        int resolution = Layer.COUNTRY.intValue();
        double nextDetailDistance = FIRST_DETAIL_DISTANCE;
        while (resolution < Layer.CUPBOARD.intValue() && distanceToSurface <= nextDetailDistance) {
            resolution++;
            nextDetailDistance /= H3_REFINEMENT;
        }
        return resolution;
    }
}
