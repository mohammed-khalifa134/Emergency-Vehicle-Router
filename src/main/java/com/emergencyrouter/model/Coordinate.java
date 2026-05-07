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

    /**
     * Returns the latitude coordinate.
     *
     * <p>Use this method when a routing algorithm calculates distance between
     * two points.</p>
     *
     * @return latitude value
     */
    @Override
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude coordinate.
     *
     * <p>Use this method together with latitude for route distance estimation.</p>
     *
     * @return longitude value
     */
    @Override
    public double getLongitude() {
        return longitude;
    }
}
