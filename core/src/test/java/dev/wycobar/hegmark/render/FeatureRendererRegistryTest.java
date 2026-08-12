package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeatureRendererRegistryTest {
    @Test
    void findsRendererForRegisteredFeature() {
        var elevation = TestPlanetFactory.create().elevation();
        FeatureRendererRegistry registry = new FeatureRendererRegistry();
        RgbColor expected = new RgbColor(0.1f, 0.2f, 0.3f);
        registry.register(elevation, value -> expected);

        assertEquals(expected, registry.rendererFor(elevation).color(10.0));
    }

    @Test
    void rejectsDuplicateRendererForFeature() {
        var elevation = TestPlanetFactory.create().elevation();
        FeatureRendererRegistry registry = new FeatureRendererRegistry();
        registry.register(elevation, value -> new RgbColor(0.1f, 0.2f, 0.3f));

        assertThrows(IllegalArgumentException.class, () ->
            registry.register(elevation, value -> new RgbColor(0.3f, 0.2f, 0.1f))
        );
    }
}
