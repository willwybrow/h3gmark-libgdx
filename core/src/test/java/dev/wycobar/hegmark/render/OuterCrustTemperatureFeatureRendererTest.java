package dev.wycobar.hegmark.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OuterCrustTemperatureFeatureRendererTest {
    private final OuterCrustTemperatureFeatureRenderer renderer = new OuterCrustTemperatureFeatureRenderer();

    @Test
    void coloursColdCrustBlueAndHotCrustRed() {
        RgbColor cold = renderer.color(-50.0);
        RgbColor temperate = renderer.color(0.0);
        RgbColor hot = renderer.color(50.0);

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
