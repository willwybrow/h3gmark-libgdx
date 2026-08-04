package dev.wycobar.hegmark.planet;

public record PlanetLatLon(double latitudeDegrees, double longitudeDegrees) {
    public PlanetLatLon {
        if (!Double.isFinite(latitudeDegrees) || latitudeDegrees < -90.0 || latitudeDegrees > 90.0) {
            throw new IllegalArgumentException("Latitude must be finite and between -90 and 90 degrees");
        }
        if (!Double.isFinite(longitudeDegrees) || longitudeDegrees < -180.0 || longitudeDegrees > 180.0) {
            throw new IllegalArgumentException("Longitude must be finite and between -180 and 180 degrees");
        }
    }
}
