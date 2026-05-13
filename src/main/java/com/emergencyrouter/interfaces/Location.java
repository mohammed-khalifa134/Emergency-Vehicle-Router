package com.emergencyrouter.interfaces;

/**
 * Represents any geographic point used by the emergency routing system.
 *
 * <p>Interface Segregation Principle: this contract exposes only the coordinate
 * data needed by routing and dispatch code.</p>
 */
public interface Location {

    /**
     * Returns the latitude coordinate.
     *
     * @return latitude value
     */
    double getLatitude();

    /**
     * Returns the longitude coordinate.
     *
     * @return longitude value
     */
    double getLongitude();
}
