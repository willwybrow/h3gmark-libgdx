package dev.wycobar.hegmark.planet;

import java.util.List;

public interface PlanetGrid {
    CellId cellAt(PlanetLatLon coordinate, int resolution);

    PlanetLatLon center(CellId cell);

    CellGeometry geometry(CellId cell);

    CellId parent(CellId cell, int parentResolution);

    List<CellId> children(CellId cell, int childResolution);

    List<CellId> neighbors(CellId cell);

    List<CellId> disk(CellId center, int radius);

    List<CellId> cellsAtResolution(int resolution);

    int resolution(CellId cell);

    boolean isPentagon(CellId cell);

    int maximumResolution();

    long stableSeed(CellId cell, long worldSeed, String featureId, int algorithmVersion);
}
