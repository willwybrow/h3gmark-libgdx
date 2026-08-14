package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.elevation.LandFeature;
import dev.wycobar.hegmark.feature.temperature.OuterCrustTemperature;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OuterCrustTemperatureFeatureRendererTest {
    @Test
    void coloursPolarCrustBluerThanEquatorialCrust() {
        var fixture = TestPlanetFactory.create();
        var feature = new OuterCrustTemperature(fixture.elevation(), new LandFeature(fixture.elevation()));
        var renderer = new OuterCrustTemperatureFeatureRenderer(feature);
        var polarCell = fixture.planet().cellAt(new PlanetLatLon(85.0, 0.0), 5);
        var equatorialCell = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 5);

        RgbColor cold = renderer.color(polarCell);
        RgbColor warm = renderer.color(equatorialCell);

        assertTrue(cold.blue() > warm.blue());
        assertTrue(warm.red() > cold.red());
    }
}
