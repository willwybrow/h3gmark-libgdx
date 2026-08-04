package dev.wycobar.hegmark.feature;

public record ResolutionRange(int minimum, int maximum) {
    public ResolutionRange {
        if (minimum < 0 || maximum > 15 || minimum > maximum) {
            throw new IllegalArgumentException("Resolution range must be ordered and within 0 through 15");
        }
    }

    public boolean contains(int resolution) {
        return resolution >= minimum && resolution <= maximum;
    }

    public boolean contains(ResolutionRange other) {
        return minimum <= other.minimum && maximum >= other.maximum;
    }
}
