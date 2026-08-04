package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanetRayPickerTest {
    private final PlanetModel planet = new PlanetModel("Test", 6_000_000.0, 5_000_000.0, 0.0, 0L);
    private final PlanetRayPicker picker = new PlanetRayPicker();

    @Test
    void intersectsRenderedSpheroidAndReturnsPlanetCoordinates() {
        PlanetLatLon coordinate = picker.pick(
            new CartesianPoint(3.0, 0.0, 0.0),
            new CartesianPoint(-1.0, 0.0, 0.0),
            planet
        ).orElseThrow();
        assertEquals(0.0, coordinate.latitudeDegrees(), 1e-10);
        assertEquals(0.0, coordinate.longitudeDegrees(), 1e-10);
    }

    @Test
    void reportsMissAndIntersectionBehindRay() {
        assertTrue(picker.pick(
            new CartesianPoint(3.0, 3.0, 0.0),
            new CartesianPoint(1.0, 0.0, 0.0),
            planet
        ).isEmpty());
    }
}
