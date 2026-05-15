package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

/**
 * Immutable latitude/longitude value object.
 *
 * <p>Use this class for report locations and vehicle positions when a simple
 * geographic coordinate is enough. It implements {@link Location}, so services
 * can depend on the interface instead of this concrete class.</p>
 */
public record Coordinate(double latitude, double longitude) implements Location {

    /**
     * Creates a coordinate after checking the values are inside valid ranges.
     *
     * @param latitude latitude from -90 to 90
     * @param longitude longitude from -180 to 180
     */
    public Coordinate {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }

   
    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }
}
