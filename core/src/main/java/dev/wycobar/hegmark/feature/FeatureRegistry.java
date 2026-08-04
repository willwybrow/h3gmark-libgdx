package dev.wycobar.hegmark.feature;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FeatureRegistry {
    private final Map<String, Feature<?>> features = new LinkedHashMap<>();

    public FeatureRegistry(Collection<? extends Feature<?>> rootFeatures) {
        rootFeatures.forEach(this::register);
    }

    public Collection<Feature<?>> features() {
        return List.copyOf(features.values());
    }

    public Feature<?> feature(String id) {
        Feature<?> feature = features.get(id);
        if (feature == null) throw new IllegalArgumentException("Unknown feature: " + id);
        return feature;
    }

    private void register(Feature<?> feature) {
        validate(feature);
        if (features.putIfAbsent(feature.id(), feature) != null) {
            throw new IllegalArgumentException("Duplicate feature id: " + feature.id());
        }
        if (feature instanceof ProvidedFeatures provider) provider.providedFeatures().forEach(this::register);
    }

    private void validate(Feature<?> feature) {
        if (feature.id() == null || feature.id().isBlank() || feature.name() == null || feature.name().isBlank()) {
            throw new IllegalArgumentException("Feature id and name must not be blank");
        }
        if (feature.valueType() == null || feature.viewableRange() == null || feature.settableRange() == null) {
            throw new IllegalArgumentException("Feature metadata must not be null");
        }
        if (feature instanceof ComputedFeature<?> && feature.settableRange().isPresent()) {
            throw new IllegalArgumentException("Computed features must not be settable");
        }
        if (feature instanceof StoredFeature<?> && feature.settableRange().isEmpty()) {
            throw new IllegalArgumentException("Stored features must declare a settable range");
        }
        feature.settableRange().ifPresent(range -> {
            if (!feature.viewableRange().contains(range)) {
                throw new IllegalArgumentException("Feature settable range must be within its viewable range");
            }
        });
    }
}
