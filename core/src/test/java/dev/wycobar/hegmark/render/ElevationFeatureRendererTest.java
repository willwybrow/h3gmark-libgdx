package dev.wycobar.hegmark.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElevationFeatureRendererTest {
    private final ElevationFeatureRenderer renderer = new ElevationFeatureRenderer();

    @Test
    void tenThousandMetresIsWhite() {
        var actual = renderer.color(10000d);

        assertEquals(1.0f, actual.red());
        assertEquals(1.0f, actual.green());
        assertEquals(1.0f, actual.blue());
    }
    @Test
    void minusTenThousandMetresIsBlack() {
        var actual = renderer.color(-10000d);

        assertEquals(0.0f, actual.red());
        assertEquals(0.0f, actual.green());
        assertEquals(0.0f, actual.blue());
    }
    @Test
    void seaLevelIsGrey() {
        var actual = renderer.color(0d);

        assertEquals(0.5f, actual.red());
        assertEquals(0.5f, actual.green());
        assertEquals(0.5f, actual.blue());
    }
}
