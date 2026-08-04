package dev.wycobar.hegmark.feature;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FeatureRegistry {
    private final Map<String, Feature<?>> features = new LinkedHashMap<>();

    public FeatureRegistry() {

    }

    public Feature<?> feature(String id) {
        Feature<?> feature = features.get(id);
        if (feature == null) throw new IllegalArgumentException("Unknown feature: " + id);
        return feature;
    }

    public void register(ComputedFeature<?> feature) {
        features.putIfAbsent(feature.id(), feature);
    }

    public void register(StoredFeature<?> feature) {
        features.putIfAbsent(feature.id(), feature);
        feature.additionalFeatures().forEach(this::register);
    }
}
