package dev.wycobar.hegmark.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureDefinitionTest {
    @Test
    void appliesOnlyInsideInclusiveRange() {
        FeatureDefinition feature = new FeatureDefinition("climate", "Climate", 2, 6);
        assertFalse(feature.appliesAt(1));
        assertTrue(feature.appliesAt(2));
        assertTrue(feature.appliesAt(6));
        assertFalse(feature.appliesAt(7));
    }

    @Test
    void rejectsInvalidRanges() {
        assertThrows(IllegalArgumentException.class, () -> new FeatureDefinition("x", "X", 7, 6));
        assertThrows(IllegalArgumentException.class, () -> new FeatureDefinition("x", "X", -1, 6));
        assertThrows(IllegalArgumentException.class, () -> new FeatureDefinition("x", "X", 1, 16));
    }
}
