package com.emergencyrouter.service;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.strategy.RouteStrategy;

import java.util.Objects;
import java.util.Optional;

/**
 * Coordinates route calculation by delegating to a selected strategy.
 *
 * <p>Use this service whenever application workflow needs a route but should
 * not depend directly on a concrete routing algorithm.</p>
 *
 * <p>Dependency Inversion Principle: this service depends on the
 * {@link RouteStrategy} abstraction, not on a specific algorithm.</p>
 */
public final class RoutingService {
    private RouteStrategy strategy;
    private Route currentRoute;

    /**
     * Creates a routing service with an initial strategy.
     *
     * <p>Use this constructor during application startup to inject the default
     * route calculation behavior.</p>
     *
     * @param strategy initial route strategy
     */
    public RoutingService(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    /**
     * Switches the active route strategy at runtime.
     *
     * <p>Use this method when the system changes from fastest routing to
     * shortest-distance routing, or later from Dijkstra to Hub Label routing.</p>
     *
     * @param strategy new route strategy
     */
    public void setStrategy(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
    }

    /**
     * Calculates and stores the latest route.
     *
     * <p>Use this method from dispatch or simulation code after a vehicle and
     * destination are known.</p>
     *
     * @param start starting location
     * @param end destination location
     * @return calculated route
     */
    public Route calculateRoute(Location start, Location end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        currentRoute = strategy.calculateRoute(start, end);
        return currentRoute;
    }

    /**
     * Gets the currently active strategy.
     *
     * <p>Use this method in tests or diagnostics to confirm strategy switching
     * happened correctly.</p>
     *
     * @return active strategy
     */
    public RouteStrategy getStrategy() {
        return strategy;
    }

    /**
     * Gets the last calculated route if one exists.
     *
     * <p>Use this method when the caller wants to inspect the most recent route
     * without recalculating it.</p>
     *
     * @return optional latest route
     */
    public Optional<Route> getCurrentRoute() {
        return Optional.ofNullable(currentRoute);
    }
}
