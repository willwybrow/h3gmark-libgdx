package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.planet.CartesianPoint;

public record SurfaceMeshData(
    float[] fillVertices,
    float[] lineVertices,
    int cellCount,
    CartesianPoint origin
) {
}
