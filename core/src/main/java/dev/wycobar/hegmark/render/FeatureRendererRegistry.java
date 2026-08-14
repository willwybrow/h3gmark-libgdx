package dev.wycobar.hegmark.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FeatureRendererRegistry {
    private final Map<String, FeatureRenderer> renderers = new LinkedHashMap<>();

    public void register(FeatureRenderer renderer) {
        FeatureRenderer existing = renderers.putIfAbsent(renderer.id(), renderer);
        if (existing != null) throw new IllegalArgumentException("Feature renderer already registered: " + renderer.id());
    }

    public List<FeatureRenderer> availableRenderers() {
        return List.copyOf(renderers.values());
    }

    FeatureRenderer rendererFor(String rendererId) {
        FeatureRenderer renderer = renderers.get(rendererId);
        if (renderer == null) throw new IllegalArgumentException("No feature renderer registered: " + rendererId);
        return renderer;
    }
}
