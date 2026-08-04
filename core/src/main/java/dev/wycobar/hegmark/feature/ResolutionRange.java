package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.Layer;

public class ResolutionRange {
    private final Layer minimum;
    private final Layer maximum;

    public ResolutionRange(Layer minimum, Layer maximum) {
        this.minimum = Layer.smallerOf(minimum, maximum);
        this.maximum = Layer.biggerOf(minimum, maximum);
    }

    public boolean contains(Layer otherLayer) {
        return otherLayer.intValue() >= minimum.intValue() && otherLayer.intValue() <= maximum.intValue();
    }

    public boolean contains(ResolutionRange otherRange) {
        return minimum.intValue() <= otherRange.minimum.intValue() && maximum.intValue() >= otherRange.maximum.intValue();
    }

    public Layer minimum() {
        return this.minimum;
    }

    public Layer maximum() {
        return this.maximum;
    }
}
