package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;

/**
 * Strategy Pattern contract for route calculation algorithms.
 *
 * <p>Dependency Inversion Principle: services will depend on this abstraction
 * instead of concrete algorithms such as fastest, shortest, Dijkstra, or Hub
 * Label routing.</p>
 */
public interface RouteStrategy {

    /**
     * Calculates a route between two locations.
     *
     * @param start starting location
     * @param end destination location
     * @return calculated route
     */
    Route calculateRoute(Location start, Location end);
}
