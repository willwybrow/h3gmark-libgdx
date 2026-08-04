package dev.wycobar.hegmark.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import dev.wycobar.hegmark.feature.ElevationResolver;
import dev.wycobar.hegmark.feature.ElevationStyle;
import dev.wycobar.hegmark.feature.ResolvedElevation;
import dev.wycobar.hegmark.feature.RgbColor;
import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.CellGeometry;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;

import java.util.List;
import java.util.Objects;

public final class CellSurfaceMeshBuilder {
    private final PlanetGrid grid;
    private final PlanetModel planet;
    private final ElevationResolver resolver;
    private final ElevationStyle style;

    public CellSurfaceMeshBuilder(
        PlanetGrid grid,
        PlanetModel planet,
        ElevationResolver resolver,
        ElevationStyle style
    ) {
        this.grid = grid;
        this.planet = planet;
        this.resolver = resolver;
        this.style = style;
    }

    public SurfaceMeshData build(List<CellId> cells, CellId selectedCell) {
        FloatArray fills = new FloatArray(false, Math.max(64, cells.size() * 72));
        FloatArray lines = new FloatArray(false, Math.max(64, cells.size() * 56));
        for (CellId cell : cells) appendCell(fills, lines, cell, Objects.equals(cell, selectedCell));
        return new SurfaceMeshData(fills.toArray(), lines.toArray(), cells.size());
    }

    private void appendCell(FloatArray fills, FloatArray lines, CellId cell, boolean selected) {
        CellGeometry geometry = grid.geometry(cell);
        ResolvedElevation elevation = resolver.resolve(cell, planet);
        RgbColor rgb = elevation.applicable()
            ? style.color(elevation.meters(), planet.seaLevelMeters())
            : new RgbColor(0.25f, 0.25f, 0.28f);
        float fillColor = Color.toFloatBits(rgb.red(), rgb.green(), rgb.blue(), 1.0f);
        float lineColor = selected
            ? Color.toFloatBits(1.0f, 0.85f, 0.1f, 1.0f)
            : Color.toFloatBits(0.03f, 0.05f, 0.06f, 0.72f);
        Vector3 center = point(geometry.center(), 1.0f);
        List<PlanetLatLon> boundary = geometry.boundary();
        for (int index = 0; index < boundary.size(); index++) {
            Vector3 first = point(boundary.get(index), 1.0f);
            Vector3 second = point(boundary.get((index + 1) % boundary.size()), 1.0f);
            appendOutwardTriangle(fills, center, first, second, fillColor);
            appendVertex(lines, point(boundary.get(index), 1.0015f), lineColor);
            appendVertex(lines, point(boundary.get((index + 1) % boundary.size()), 1.0015f), lineColor);
        }
    }

    private void appendOutwardTriangle(FloatArray values, Vector3 center, Vector3 first, Vector3 second, float color) {
        Vector3 normal = new Vector3(first).sub(center).crs(new Vector3(second).sub(center));
        appendVertex(values, center, color);
        if (normal.dot(center) >= 0.0f) {
            appendVertex(values, first, color);
            appendVertex(values, second, color);
        } else {
            appendVertex(values, second, color);
            appendVertex(values, first, color);
        }
    }

    private Vector3 point(PlanetLatLon coordinate, float scale) {
        CartesianPoint point = planet.toRenderCartesian(coordinate);
        return new Vector3((float) point.x() * scale, (float) point.y() * scale, (float) point.z() * scale);
    }

    private void appendVertex(FloatArray values, Vector3 point, float color) {
        values.add(point.x, point.y, point.z, color);
    }
}
