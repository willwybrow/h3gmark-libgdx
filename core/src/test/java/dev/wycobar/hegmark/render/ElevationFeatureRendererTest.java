package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.FeatureMutationStatus;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElevationFeatureRendererTest {
    private final TestPlanetFactory.Fixture fixture = TestPlanetFactory.create();
    private final Cell cell = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 3);
    private final ElevationFeatureRenderer renderer = new ElevationFeatureRenderer(fixture.elevation());

    @Test
    void tenThousandMetresIsWhite() {
        assertEquals(new RgbColor(1.0f, 1.0f, 1.0f), color(10_000.0));
    }

    @Test
    void minusTenThousandMetresIsBlack() {
        assertEquals(new RgbColor(0.0f, 0.0f, 0.0f), color(-10_000.0));
    }

    @Test
    void eightThousandMetresIsWhite() {
        assertEquals(new RgbColor(1.0f, 1.0f, 1.0f), color(8_000.0));
    }

    @Test
    void minusEightThousandMetresIsBlack() {
        assertEquals(new RgbColor(0.0f, 0.0f, 0.0f), color(-8_000.0));
    }

    @Test
    void seaLevelIsGrey() {
        assertEquals(new RgbColor(0.5f, 0.5f, 0.5f), color(0.0));
    }

    private RgbColor color(double elevationMeters) {
        assertEquals(
            FeatureMutationStatus.APPLIED,
            fixture.elevation().overwriteAt(cell, elevationMeters, true).status()
        );
        return renderer.color(cell);
    }
}
