package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.FeatureMutationStatus;
import dev.wycobar.hegmark.feature.elevation.LandFeature;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandFeatureRendererTest {
    @Test
    void coloursLandGreenAndSeaBlue() {
        var fixture = TestPlanetFactory.create();
        Cell cell = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 3);
        LandFeatureRenderer renderer = new LandFeatureRenderer(new LandFeature(fixture.elevation()));

        assertEquals(FeatureMutationStatus.APPLIED, fixture.elevation().overwriteAt(cell, 1.0, true).status());
        RgbColor land = renderer.color(cell);
        assertEquals(FeatureMutationStatus.APPLIED, fixture.elevation().overwriteAt(cell, -1.0, true).status());
        RgbColor sea = renderer.color(cell);

        assertTrue(land.green() > land.blue());
        assertTrue(sea.blue() > sea.green());
    }
}
