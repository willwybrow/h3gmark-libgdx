package dev.wycobar.hegmark.feature;

public record ExplicitFeatureValue<T>(StoredFeature<T> feature, T value) {
    public ExplicitFeatureValue {
        value = feature.valueType().cast(value);
    }
}
