package dev.wycobar.hegmark.planet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlanetModelTest {
    private final PlanetModel planet = new PlanetModel("Test", 6_000_000.0, 5_000_000.0, 0.0, 42L);

    @Test
    void convertsEquatorAndPoleThroughFictionalRadii() {
        assertEquals(new CartesianPoint(6_000_000.0, 0.0, 0.0), planet.toCartesian(new PlanetLatLon(0.0, 0.0)));

        CartesianPoint pole = planet.toCartesian(new PlanetLatLon(90.0, 0.0));
        assertEquals(0.0, pole.x(), 1e-8);
        assertEquals(5_000_000.0, pole.y(), 1e-8);
        assertEquals(0.0, pole.z(), 1e-8);
    }

    @Test
    void normalizesOnlyAtRenderingBoundary() {
        CartesianPoint point = planet.toRenderCartesian(new PlanetLatLon(90.0, 0.0));
        assertEquals(5.0 / 6.0, point.y(), 1e-12);
        assertEquals(5.0 / 6.0, planet.renderPolarRadius(), 1e-12);
    }

    @Test
    void rejectsInvalidPlanetAndCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new PlanetModel("", 1.0, 1.0, 0.0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new PlanetLatLon(91.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new PlanetLatLon(0.0, 181.0));
    }
}
