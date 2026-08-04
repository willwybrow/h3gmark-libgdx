package dev.wycobar.hegmark.planet;

import java.util.Objects;

public record PlanetModel(
    String name,
    double equatorialRadiusMeters,
    double polarRadiusMeters,
    double seaLevelMeters,
    long worldSeed
) {
    public PlanetModel {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("Planet name must not be blank");
        if (!Double.isFinite(equatorialRadiusMeters) || equatorialRadiusMeters <= 0.0) {
            throw new IllegalArgumentException("Equatorial radius must be positive and finite");
        }
        if (!Double.isFinite(polarRadiusMeters) || polarRadiusMeters <= 0.0) {
            throw new IllegalArgumentException("Polar radius must be positive and finite");
        }
        if (!Double.isFinite(seaLevelMeters)) throw new IllegalArgumentException("Sea level must be finite");
    }

    public CartesianPoint toCartesian(PlanetLatLon coordinate) {
        double latitude = Math.toRadians(coordinate.latitudeDegrees());
        double longitude = Math.toRadians(coordinate.longitudeDegrees());
        double horizontal = Math.cos(latitude);
        return new CartesianPoint(
            equatorialRadiusMeters * horizontal * Math.cos(longitude),
            polarRadiusMeters * Math.sin(latitude),
            equatorialRadiusMeters * horizontal * Math.sin(longitude)
        );
    }

    public CartesianPoint toRenderCartesian(PlanetLatLon coordinate) {
        CartesianPoint physical = toCartesian(coordinate);
        return new CartesianPoint(
            physical.x() / equatorialRadiusMeters,
            physical.y() / equatorialRadiusMeters,
            physical.z() / equatorialRadiusMeters
        );
    }

    public double renderPolarRadius() {
        return polarRadiusMeters / equatorialRadiusMeters;
    }
}
