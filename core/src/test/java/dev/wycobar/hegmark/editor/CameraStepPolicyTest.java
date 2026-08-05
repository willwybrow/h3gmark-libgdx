package dev.wycobar.hegmark.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraStepPolicyTest {
    private final CameraStepPolicy policy = new CameraStepPolicy();

    @Test
    void rotatesBySmallerLongitudeStepsCloserToTheSurface() {
        assertEquals(10.0f, policy.longitudeDegrees(5.0));
        assertEquals(9.0f, policy.longitudeDegrees(1.8));
        assertEquals(2.5f, policy.longitudeDegrees(0.5));
        assertEquals(0.5f, policy.longitudeDegrees(0.1));
        assertEquals(0.01f, policy.longitudeDegrees(0.0));
        assertTrue(policy.longitudeDegrees(0.1) < policy.longitudeDegrees(1.0));
    }

    @Test
    void rejectsInvalidDistance() {
        assertThrows(IllegalArgumentException.class, () -> policy.longitudeDegrees(-0.1));
        assertThrows(IllegalArgumentException.class, () -> policy.longitudeDegrees(Double.NaN));
    }
}
