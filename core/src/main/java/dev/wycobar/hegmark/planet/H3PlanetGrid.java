package dev.wycobar.hegmark.planet;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class H3PlanetGrid implements PlanetGrid {
    private final H3Core h3;

    public H3PlanetGrid() {
        try {
            h3 = H3Core.newInstance();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load H3", exception);
        }
    }

    @Override
    public CellId cellAt(PlanetLatLon coordinate, int resolution) {
        return new CellId(h3.latLngToCell(coordinate.latitudeDegrees(), coordinate.longitudeDegrees(), resolution));
    }

    @Override
    public PlanetLatLon center(CellId cell) {
        return coordinate(h3.cellToLatLng(cell.value()));
    }

    @Override
    public CellGeometry geometry(CellId cell) {
        List<PlanetLatLon> boundary = h3.cellToBoundary(cell.value()).stream().map(this::coordinate).toList();
        return new CellGeometry(center(cell), boundary);
    }

    @Override
    public CellId parent(CellId cell, int parentResolution) {
        return new CellId(h3.cellToParent(cell.value(), parentResolution));
    }

    @Override
    public List<CellId> children(CellId cell, int childResolution) {
        return h3.cellToChildren(cell.value(), childResolution).stream().map(CellId::new).toList();
    }

    @Override
    public List<CellId> siblings(CellId cell) {
        var resolution = h3.getResolution(cell.value());
        if (resolution == 0) {
            return Collections.emptyList();
        }
        return h3.cellToChildren(h3.cellToParent(cell.value(), resolution - 1), resolution)
            .stream()
            .filter(id -> id != cell.value())
            .map(CellId::new)
            .toList();
    }

    @Override
    public List<CellId> neighbours(CellId cell) {
        return h3.gridDisk(cell.value(), 1).stream()
            .filter(value -> value != cell.value())
            .map(CellId::new)
            .toList();
    }

    @Override
    public List<CellId> disk(CellId center, int radius) {
        if (radius < 0) throw new IllegalArgumentException("Disk radius must not be negative");
        return h3.gridDisk(center.value(), radius).stream().map(CellId::new).toList();
    }

    @Override
    public List<CellId> cellsAtResolution(int resolution) {
        if (resolution < 0 || resolution > 2) {
            throw new IllegalArgumentException("Whole-planet enumeration is restricted to resolutions 0 through 2");
        }
        if (resolution == 0) return h3.getRes0Cells().stream().map(CellId::new).toList();
        return h3.getRes0Cells().stream()
            .flatMap(cell -> h3.cellToChildren(cell, resolution).stream())
            .map(CellId::new)
            .toList();
    }

    @Override
    public int resolution(CellId cell) {
        return h3.getResolution(cell.value());
    }

    @Override
    public int maximumResolution() {
        return 15;
    }

    @Override
    public long stableSeed(CellId cell, long worldSeed, String featureId, int algorithmVersion) {
        long hash = worldSeed ^ cell.value();
        hash = Long.rotateLeft(hash, 21) ^ featureId.hashCode();
        hash = Long.rotateLeft(hash, 17) ^ algorithmVersion;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        return hash;
    }

    private PlanetLatLon coordinate(LatLng coordinate) {
        return new PlanetLatLon(coordinate.lat, coordinate.lng);
    }
}
