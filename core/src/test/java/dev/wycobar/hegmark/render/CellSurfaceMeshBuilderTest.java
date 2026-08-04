package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.ElevationStyle;
import dev.wycobar.hegmark.planet.CellGeometry;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetModel;
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
        builder = new CellSurfaceMeshBuilder(fixture.planet(), fixture.elevation(), new ElevationStyle());
    }

    @Test
    void triangulatesVariableBoundaryAsCenterFanAndAddsEveryOutlineEdge() {
        CellId pentagon = grid.cellsAtResolution(2).stream().filter(grid::isPentagon).findFirst().orElseThrow();
        CellGeometry geometry = grid.geometry(pentagon);
        SurfaceMeshData mesh = builder.build(List.of(pentagon), pentagon);

        assertEquals(geometry.boundary().size() * 3 * 4, mesh.fillVertices().length);
        assertEquals(geometry.boundary().size() * 2 * 4, mesh.lineVertices().length);
        assertEquals(1, mesh.cellCount());
    }

    @Test
    void emitsOutwardFacingTrianglesForBackFaceCulling() {
        CellId cell = grid.cellsAtResolution(2).getFirst();
        float[] vertices = builder.build(List.of(cell), null).fillVertices();

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
            assertTrue(nx * cx + ny * cy + nz * cz >= -1e-7f);
        }
    }
}
