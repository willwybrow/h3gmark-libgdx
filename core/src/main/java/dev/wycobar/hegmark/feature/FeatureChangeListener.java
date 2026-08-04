package dev.wycobar.hegmark.feature;

@FunctionalInterface
public interface FeatureChangeListener {
    void featureValuesChanged(FeatureValuesChanged event);
}
