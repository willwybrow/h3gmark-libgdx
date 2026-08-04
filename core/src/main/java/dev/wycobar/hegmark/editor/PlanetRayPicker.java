package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;

import java.util.Optional;

public final class PlanetRayPicker {
    public Optional<PlanetLatLon> pick(CartesianPoint origin, CartesianPoint direction, PlanetModel planet) {
        double polarRadius = planet.renderPolarRadius();
        if (!finite(origin) || !finite(direction) || !Double.isFinite(polarRadius) || polarRadius <= 0.0) {
            return Optional.empty();
        }
        double a = direction.x() * direction.x() + direction.z() * direction.z()
            + direction.y() * direction.y() / (polarRadius * polarRadius);
        double b = 2.0 * (origin.x() * direction.x() + origin.z() * direction.z()
            + origin.y() * direction.y() / (polarRadius * polarRadius));
        double c = origin.x() * origin.x() + origin.z() * origin.z()
            + origin.y() * origin.y() / (polarRadius * polarRadius) - 1.0;
        if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c) || a <= 0.0) {
            return Optional.empty();
        }
        double discriminant = b * b - 4.0 * a * c;
        if (!Double.isFinite(discriminant) || discriminant < 0.0) return Optional.empty();

        double root = Math.sqrt(discriminant);
        double q = -0.5 * (b + Math.copySign(root, b));
        double first = q / a;
        double second = q == 0.0 ? 0.0 : c / q;
        double distance = nearestNonNegative(first, second);
        if (!Double.isFinite(distance)) return Optional.empty();

        double x = origin.x() + direction.x() * distance;
        double y = origin.y() + direction.y() * distance;
        double z = origin.z() + direction.z() * distance;
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return Optional.empty();
        double longitude = Math.toDegrees(Math.atan2(z, x));
        double latitude = Math.toDegrees(Math.atan2(y / polarRadius, Math.hypot(x, z)));
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) return Optional.empty();
        return Optional.of(new PlanetLatLon(latitude, longitude));
    }

    private double nearestNonNegative(double first, double second) {
        if (first < 0.0) return second >= 0.0 ? second : Double.NaN;
        if (second < 0.0) return first;
        return Math.min(first, second);
    }

    private boolean finite(CartesianPoint point) {
        return Double.isFinite(point.x()) && Double.isFinite(point.y()) && Double.isFinite(point.z());
    }
}
