package dev.wycobar.hegmark.render;

@FunctionalInterface
public interface FeatureRenderer<T> {
    RgbColor color(T value);
}
