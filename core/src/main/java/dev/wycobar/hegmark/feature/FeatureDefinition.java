package dev.wycobar.hegmark.feature;

import java.util.Objects;

public record FeatureDefinition(String id, String name, int minimumResolution, int maximumResolution) {
    public FeatureDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        if (id.isBlank() || name.isBlank()) throw new IllegalArgumentException("Feature id and name must not be blank");
        if (minimumResolution < 0 || maximumResolution > 15 || minimumResolution > maximumResolution) {
            throw new IllegalArgumentException("Feature resolution range must be ordered and within 0 through 15");
        }
    }

    public boolean appliesAt(int resolution) {
        return resolution >= minimumResolution && resolution <= maximumResolution;
    }
}
