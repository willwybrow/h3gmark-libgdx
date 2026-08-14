package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellSurfaceMeshBuilderTest {
    private PlanetGrid grid;
    private CellSurfaceMeshBuilder builder;

    @BeforeEach
    void setUp() {
        var fixture = TestPlanetFactory.create(12L);
        grid = fixture.grid();
        FeatureRendererRegistry renderers = new FeatureRendererRegistry();
        FeatureRenderer elevationRenderer = new ElevationFeatureRenderer(fixture.elevation());
        renderers.register(elevationRenderer);
        builder = new CellSurfaceMeshBuilder(fixture.planet(), renderers.rendererFor(elevationRenderer.id()));
    }

    @Test
    void emitsOutwardFacingTrianglesForBackFaceCulling() {
        CellId cell = grid.cellsAtResolution(2).getFirst();
        SurfaceMeshData mesh = builder.build(List.of(cell), null);
        float[] vertices = mesh.fillVertices();

        for (int offset = 0; offset < vertices.length; offset += 12) {
            float cx = vertices[offset];
            float cy = vertices[offset + 1];
            float cz = vertices[offset + 2];
            float ax = vertices[offset + 4] - cx;
            float ay = vertices[offset + 5] - cy;
            float az = vertices[offset + 6] - cz;
            float bx = vertices[offset + 8] - cx;
            float by = vertices[offset + 9] - cy;
            float bz = vertices[offset + 10] - cz;
            float nx = ay * bz - az * by;
            float ny = az * bx - ax * bz;
            float nz = ax * by - ay * bx;
            double globalX = cx + mesh.origin().x();
            double globalY = cy + mesh.origin().y();
            double globalZ = cz + mesh.origin().z();
            assertTrue(nx * globalX + ny * globalY + nz * globalZ >= -1e-7);
        }
    }

    @Test
    void preservesDistinctGeometryAtMaximumLayer() {
        CellId cell = grid.cellAt(new dev.wycobar.hegmark.planet.PlanetLatLon(0.0, 0.0), 15);
        float[] vertices = builder.build(List.of(cell), null).fillVertices();
        double centerToBoundary = Math.sqrt(
            Math.pow(vertices[4] - vertices[0], 2)
                + Math.pow(vertices[5] - vertices[1], 2)
                + Math.pow(vertices[6] - vertices[2], 2)
        );
        double boundaryEdge = Math.sqrt(
            Math.pow(vertices[8] - vertices[4], 2)
                + Math.pow(vertices[9] - vertices[5], 2)
                + Math.pow(vertices[10] - vertices[6], 2)
        );
        assertTrue(centerToBoundary > 0.0);
        assertTrue(boundaryEdge > 0.0);
    }
}
