package dev.wycobar.hegmark.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LandFeatureRendererTest {
    private final LandFeatureRenderer renderer = new LandFeatureRenderer();

    @Test
    void coloursLandGreenAndSeaBlue() {
        RgbColor land = renderer.color(true);
        RgbColor sea = renderer.color(false);

        assertTrue(land.green() > land.blue());
        assertTrue(sea.blue() > sea.green());
    }
}
