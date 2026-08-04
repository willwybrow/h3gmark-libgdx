package dev.wycobar.hegmark.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LodPolicyTest {
    private final LodPolicy policy = new LodPolicy();

    @Test
    void increasesDetailAsCameraApproaches() {
        assertEquals(2, policy.resolution(2.0));
        assertEquals(3, policy.resolution(0.3));
        assertEquals(4, policy.resolution(0.1));
        assertEquals(8, policy.resolution(0.002));
    }

    @Test
    void changesOnlyAtCoverageSafeSurfaceDistances() {
        assertEquals(2, policy.resolution(0.501));
        assertEquals(3, policy.resolution(0.5));
        assertEquals(7, policy.resolution(0.004));
        assertEquals(8, policy.resolution(0.0038));
    }
}
