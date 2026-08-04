package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.feature.elevation.ElevationStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ElevationStyleTest {
    private final ElevationStyle style = new ElevationStyle();

    @Test
    void waterIsBlueAndLandIsGreenRelativeToSeaLevel() {
        RgbColor water = style.color(-1.0, 0.0);
        RgbColor land = style.color(1.0, 0.0);
        assertTrue(water.blue() > water.green());
        assertTrue(land.green() > land.blue());
    }

    @Test
    void seaLevelOffsetChangesClassification() {
        RgbColor water = style.color(50.0, 100.0);
        assertTrue(water.blue() > water.green());
    }
}
