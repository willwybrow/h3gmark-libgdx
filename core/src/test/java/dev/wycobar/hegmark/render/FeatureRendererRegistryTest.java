package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.elevation.LandFeature;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeatureRendererRegistryTest {
    @Test
    void findsRendererByRendererId() {
        var elevation = TestPlanetFactory.create().elevation();
        FeatureRendererRegistry registry = new FeatureRendererRegistry();
        FeatureRenderer renderer = new ElevationFeatureRenderer(elevation);
        registry.register(renderer);

        assertSame(renderer, registry.rendererFor(renderer.id()));
    }

    @Test
    void rejectsDuplicateRendererId() {
        var elevation = TestPlanetFactory.create().elevation();
        FeatureRendererRegistry registry = new FeatureRendererRegistry();
        registry.register(new ElevationFeatureRenderer(elevation));

        assertThrows(IllegalArgumentException.class, () ->
            registry.register(new ElevationFeatureRenderer(elevation))
        );
    }

    @Test
    void exposesAvailableRenderersInRegistrationOrder() {
        var elevation = TestPlanetFactory.create().elevation();
        var land = new LandFeature(elevation);
        FeatureRendererRegistry registry = new FeatureRendererRegistry();
        FeatureRenderer elevationRenderer = new ElevationFeatureRenderer(elevation);
        FeatureRenderer landRenderer = new LandFeatureRenderer(land);
        registry.register(elevationRenderer);
        registry.register(landRenderer);

        assertEquals(
            java.util.List.of(elevationRenderer, landRenderer),
            registry.availableRenderers()
        );
    }
}
