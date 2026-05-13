package com.emergencyrouter.service;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.TrafficData;
import com.emergencyrouter.observer.Observer;
import com.emergencyrouter.strategy.HubLabelRouteStrategy;
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
public final class RoutingService implements Observer<TrafficData> {
    private RouteStrategy strategy;
    private Route currentRoute;
    private Location lastStart;
    private Location lastEnd;
    private final Graph graph;

    /**
     * Creates a routing service with an initial strategy.
     *
     * <p>Use this constructor during application startup to inject the default
     * route calculation behavior.</p>
     *
     * @param strategy initial route strategy
     */
    public RoutingService(RouteStrategy strategy) {
        this(strategy, null);
    }

    /**
     * Creates a graph-aware routing service with an initial strategy.
     *
     * <p>Use this constructor when the service should react to traffic updates.
     * The graph is needed so traffic data can update road weights and closures.</p>
     *
     * @param strategy initial route strategy
     * @param graph road graph affected by traffic updates
     */
    public RoutingService(RouteStrategy strategy, Graph graph) {
        this.graph = graph;
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

        lastStart = start;
        lastEnd = end;
        currentRoute = strategy.calculateRoute(start, end);
        return currentRoute;
    }

    /**
     * Reacts to traffic updates from {@link TrafficService}.
     *
     * <p>Use this method through the Observer Pattern. It updates the graph,
     * refreshes Hub Label data when needed, and recalculates the current route
     * if this service has already calculated one before.</p>
     *
     * @param data latest traffic update
     */
    @Override
    public void update(TrafficData data) {
        Objects.requireNonNull(data, "data must not be null");

        if (graph == null) {
            System.out.println("Traffic update received, but no graph is configured for routing.");
            return;
        }

        graph.updateTraffic(data.getRoadId(), data.getCongestionLevel(), data.isRoadClosed());
        refreshHubLabelsIfNeeded();

        if (lastStart == null || lastEnd == null) {
            return;
        }

        System.out.println("Recalculating route...");
        currentRoute = strategy.calculateRoute(lastStart, lastEnd);
        System.out.println("New route assigned.");
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

    /**
     * Refreshes preprocessed Hub Label data when Hub Label routing is active.
     *
     * <p>Use this helper after graph traffic changes. Preprocessed labels become
     * stale when roads close or congestion changes.</p>
     */
    private void refreshHubLabelsIfNeeded() {
        if (strategy instanceof HubLabelRouteStrategy hubLabelRouteStrategy) {
            HubLabelPreprocessor preprocessor = new HubLabelPreprocessor(graph);
            hubLabelRouteStrategy.refreshLabels(preprocessor.preprocess());
        }
    }
}
