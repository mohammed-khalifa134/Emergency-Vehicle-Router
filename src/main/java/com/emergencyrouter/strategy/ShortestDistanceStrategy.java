package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;

import java.util.Objects;

/**
 * Basic strategy that prioritizes lower physical distance.
 *
 * <p>Use this class when the system wants the shortest direct coordinate
 * distance. It is intentionally simple for learning before Dijkstra and Hub
 * Labels are added.</p>
 */
public final class ShortestDistanceStrategy implements RouteStrategy {
    private static final double CITY_AVERAGE_SPEED_KPH = 50.0;

    /**
     * Calculates a direct shortest-distance route estimate.
     *
     * <p>Use this method through {@link com.emergencyrouter.service.RoutingService}
     * to demonstrate runtime strategy switching.</p>
     *
     * @param start starting location
     * @param end destination location
     * @return calculated route
     */
    @Override
    public Route calculateRoute(Location start, Location end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        System.out.println("Calculating shortest-distance route...");

        double distance = RouteCalculationSupport.estimateDistanceInKilometers(start, end);
        double time = RouteCalculationSupport.estimateTimeInMinutes(distance, CITY_AVERAGE_SPEED_KPH);

        return RouteCalculationSupport.buildDirectRoute(start, end, distance, time);
    }
}
