package dev.wycobar.hegmark.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeaTemperatureFeatureRendererTest {
    private final SeaTemperatureFeatureRenderer renderer = new SeaTemperatureFeatureRenderer();

    @Test
    void coloursColdWaterBlueAndHotWaterRed() {
        RgbColor cold = renderer.color(-2.0);
        RgbColor temperate = renderer.color(16.5);
        RgbColor hot = renderer.color(35.0);

        assertTrue(cold.blue() > cold.red());
        assertTrue(temperate.green() > cold.green());
        assertTrue(hot.red() > hot.blue());
    }

    @Test
    void clampsTemperaturesOutsideTheScale() {
        assertTrue(renderer.color(-100.0).blue() > renderer.color(-100.0).red());
        assertTrue(renderer.color(100.0).red() > renderer.color(100.0).blue());
    }
}
