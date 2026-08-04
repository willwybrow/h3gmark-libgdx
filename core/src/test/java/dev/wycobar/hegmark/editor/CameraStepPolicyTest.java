package dev.wycobar.hegmark.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraStepPolicyTest {
    private final CameraStepPolicy policy = new CameraStepPolicy();

    @Test
    void rotatesBySmallerLongitudeStepsAtFinerResolutions() {
        assertEquals(30.0f, policy.longitudeDegrees(2));
        assertTrue(policy.longitudeDegrees(4) < policy.longitudeDegrees(3));
        assertTrue(policy.longitudeDegrees(8) < 0.1f);
        assertTrue(policy.longitudeDegrees(15) < 0.001f);
    }

    @Test
    void rejectsInvalidGridResolution() {
        assertThrows(IllegalArgumentException.class, () -> policy.longitudeDegrees(-1));
        assertThrows(IllegalArgumentException.class, () -> policy.longitudeDegrees(16));
    }
}
