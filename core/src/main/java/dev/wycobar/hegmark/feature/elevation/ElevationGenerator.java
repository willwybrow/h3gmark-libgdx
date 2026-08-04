package dev.wycobar.hegmark.feature.elevation;

import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;

final class ElevationGenerator {
    static final int ALGORITHM_VERSION = 1;

    double generate(PlanetModel planet, long stableCellSeed, PlanetLatLon center) {
        double latitude = Math.toRadians(center.latitudeDegrees());
        double longitude = Math.toRadians(center.longitudeDegrees());
        double phase = phase(planet.worldSeed());
        double secondaryPhase = phase(stableCellSeed);
        double continents = 1_550.0 * Math.sin(longitude * 2.0 + phase) * Math.cos(latitude * 1.4);
        double ridges = 700.0 * Math.cos(latitude * 3.0 - phase) * Math.sin(longitude * 4.0 + secondaryPhase);
        return continents + ridges - 250.0;
    }

    private double phase(long seed) {
        long mixed = seed ^ (seed >>> 33);
        return ((mixed & 0xffffL) / 65535.0) * Math.PI * 2.0;
    }
}
