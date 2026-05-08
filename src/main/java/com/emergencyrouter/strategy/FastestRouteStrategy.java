package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;

import java.util.Objects;

/**
 * Basic strategy that prioritizes lower estimated travel time.
 *
 * <p>Use this class when the system wants a simple fastest-route estimate before
 * advanced graph algorithms are introduced. It demonstrates the Strategy
 * Pattern with a readable calculation.</p>
 */
public final class FastestRouteStrategy implements RouteStrategy {
    private static final double EMERGENCY_AVERAGE_SPEED_KPH = 80.0;

    /**
     * Calculates a direct fastest route estimate.
     *
     * <p>Use this method through {@link com.emergencyrouter.service.RoutingService}
     * so the service can switch strategies without knowing this concrete class.</p>
     *
     * @param start starting location
     * @param end destination location
     * @return calculated route
     */
    @Override
    public Route calculateRoute(Location start, Location end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        System.out.println("Calculating fastest route...");

        double distance = RouteCalculationSupport.estimateDistanceInKilometers(start, end);
        double time = RouteCalculationSupport.estimateTimeInMinutes(distance, EMERGENCY_AVERAGE_SPEED_KPH);

        return RouteCalculationSupport.buildDirectRoute(start, end, distance, time);
    }
}
