package dev.wycobar.hegmark.render;

import dev.wycobar.hegmark.feature.Feature;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FeatureRendererRegistry {
    private final Map<String, FeatureRenderer<?>> renderers = new LinkedHashMap<>();

    public <T> void register(Feature<T> feature, FeatureRenderer<T> renderer) {
        FeatureRenderer<?> existing = renderers.putIfAbsent(feature.id(), renderer);
        if (existing != null) throw new IllegalArgumentException("Feature renderer already registered: " + feature.id());
    }

    public <T> FeatureRenderer<T> rendererFor(Feature<T> feature) {
        FeatureRenderer<?> renderer = renderers.get(feature.id());
        if (renderer == null) throw new IllegalArgumentException("No renderer registered for feature: " + feature.id());
        return cast(renderer);
    }

    @SuppressWarnings("unchecked")
    private <T> FeatureRenderer<T> cast(FeatureRenderer<?> renderer) {
        return (FeatureRenderer<T>) renderer;
    }
}
