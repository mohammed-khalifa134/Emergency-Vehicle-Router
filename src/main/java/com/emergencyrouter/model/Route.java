package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

import java.util.List;
import java.util.Objects;

/**
 * Represents a calculated route between two locations.
 *
 * <p>Use this class whenever a routing strategy needs to return a result to the
 * rest of the system. It keeps the ordered path, total distance, and estimated
 * travel time together as one immutable value.</p>
 */
public final class Route {
    private final List<Location> path;
    private final double distance;
    private final double time;

    /**
     * Creates a route result.
     *
     * @param path ordered locations from start to destination
     * @param distance total route distance
     * @param time estimated travel time
     */
    public Route(List<Location> path, double distance, double time) {
        this.path = List.copyOf(Objects.requireNonNull(path, "path must not be null"));
        this.distance = distance;
        this.time = time;
    }

    /**
     * Gets the ordered path.
     *
     * <p>Use this method when printing route steps or when a vehicle needs to
     * know the sequence of locations it should follow.</p>
     *
     * @return immutable list of route locations
     */
    public List<Location> getPath() {
        return path;
    }

    /**
     * Gets the total route distance.
     *
     * <p>Use this method to compare shortest-route results or show route length
     * in the console simulation.</p>
     *
     * @return total distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Gets the estimated travel time.
     *
     * <p>Use this method to compare fastest-route results or display expected
     * emergency response time.</p>
     *
     * @return estimated time
     */
    public double getTime() {
        return time;
    }
}
