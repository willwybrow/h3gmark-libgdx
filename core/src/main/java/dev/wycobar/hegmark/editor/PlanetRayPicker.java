package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;

import java.util.Optional;

public final class PlanetRayPicker {
    public Optional<PlanetLatLon> pick(CartesianPoint origin, CartesianPoint direction, PlanetModel planet) {
        double polarRadius = planet.renderPolarRadius();
        double a = direction.x() * direction.x() + direction.z() * direction.z()
            + direction.y() * direction.y() / (polarRadius * polarRadius);
        double b = 2.0 * (origin.x() * direction.x() + origin.z() * direction.z()
            + origin.y() * direction.y() / (polarRadius * polarRadius));
        double c = origin.x() * origin.x() + origin.z() * origin.z()
            + origin.y() * origin.y() / (polarRadius * polarRadius) - 1.0;
        double discriminant = b * b - 4.0 * a * c;
        if (discriminant < 0.0) return Optional.empty();

        double root = Math.sqrt(discriminant);
        double near = (-b - root) / (2.0 * a);
        double far = (-b + root) / (2.0 * a);
        double distance = near >= 0.0 ? near : far;
        if (distance < 0.0) return Optional.empty();

        double x = origin.x() + direction.x() * distance;
        double y = origin.y() + direction.y() * distance;
        double z = origin.z() + direction.z() * distance;
        double longitude = Math.toDegrees(Math.atan2(z, x));
        double latitude = Math.toDegrees(Math.atan2(y / polarRadius, Math.hypot(x, z)));
        return Optional.of(new PlanetLatLon(latitude, longitude));
    }
}
