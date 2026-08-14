package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.planet.Cell;

public interface FeatureRenderer {
    default String id() {
        return getClass().getName();
    }

    default String name() {
        return getClass().getSimpleName();
    }

    RgbColor color(Cell cell);
}
