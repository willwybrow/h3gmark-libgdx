package dev.wycobar.hegmark.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.feature.elevation.ElevationStyle;
import dev.wycobar.hegmark.feature.RgbColor;
import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.CellGeometry;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;
import dev.wycobar.hegmark.planet.Planet;
import dev.wycobar.hegmark.planet.Cell;

import java.util.List;
import java.util.Objects;

public final class CellSurfaceMeshBuilder {
    private final Planet world;
    private final PlanetModel planet;
    private final ElevationFeature elevationFeature;
    private final ElevationStyle style;

    public CellSurfaceMeshBuilder(
        Planet world,
        ElevationFeature elevationFeature,
        ElevationStyle style
    ) {
        this.world = world;
        this.planet = world.definition();
        this.elevationFeature = elevationFeature;
        this.style = style;
    }

    public SurfaceMeshData build(List<CellId> cells, CellId selectedCell) {
        FloatArray fills = new FloatArray(false, Math.max(64, cells.size() * 72));
        FloatArray lines = new FloatArray(false, Math.max(64, cells.size() * 56));
        CartesianPoint origin = cells.isEmpty()
            ? new CartesianPoint(0.0, 0.0, 0.0)
            : planet.toRenderCartesian(world.cell(cells.getFirst()).center());
        for (CellId cell : cells) appendCell(fills, lines, cell, Objects.equals(cell, selectedCell), origin);
        return new SurfaceMeshData(fills.toArray(), lines.toArray(), cells.size(), origin);
    }

    private void appendCell(
        FloatArray fills,
        FloatArray lines,
        CellId cell,
        boolean selected,
        CartesianPoint origin
    ) {
        Cell domainCell = world.cell(cell);
        CellGeometry geometry = domainCell.geometry();
        RgbColor rgb = style.color(elevationFeature.valueAt(domainCell), planet.seaLevelMeters());
        float fillColor = Color.toFloatBits(rgb.red(), rgb.green(), rgb.blue(), 1.0f);
        float lineColor = selected
            ? Color.toFloatBits(1.0f, 0.85f, 0.1f, 1.0f)
            : Color.toFloatBits(0.03f, 0.05f, 0.06f, 0.72f);
        CartesianPoint globalCenter = planet.toRenderCartesian(geometry.center());
        Vector3 center = localPoint(globalCenter, origin);
        Vector3 outward = vector(globalCenter);
        List<PlanetLatLon> boundary = geometry.boundary();
        for (int index = 0; index < boundary.size(); index++) {
            Vector3 first = localPoint(planet.toRenderCartesian(boundary.get(index)), origin);
            Vector3 second = localPoint(
                planet.toRenderCartesian(boundary.get((index + 1) % boundary.size())),
                origin
            );
            appendOutwardTriangle(fills, center, first, second, outward, fillColor);
            appendVertex(lines, first, lineColor);
            appendVertex(lines, second, lineColor);
        }
    }

    private void appendOutwardTriangle(
        FloatArray values,
        Vector3 center,
        Vector3 first,
        Vector3 second,
        Vector3 outward,
        float color
    ) {
        Vector3 normal = new Vector3(first).sub(center).crs(new Vector3(second).sub(center));
        appendVertex(values, center, color);
        if (normal.dot(outward) >= 0.0f) {
            appendVertex(values, first, color);
            appendVertex(values, second, color);
        } else {
            appendVertex(values, second, color);
            appendVertex(values, first, color);
        }
    }

    private Vector3 localPoint(CartesianPoint point, CartesianPoint origin) {
        return new Vector3(
            (float) (point.x() - origin.x()),
            (float) (point.y() - origin.y()),
            (float) (point.z() - origin.z())
        );
    }

    private Vector3 vector(CartesianPoint point) {
        return new Vector3((float) point.x(), (float) point.y(), (float) point.z());
    }

    private void appendVertex(FloatArray values, Vector3 point, float color) {
        values.add(point.x, point.y, point.z, color);
    }
}
