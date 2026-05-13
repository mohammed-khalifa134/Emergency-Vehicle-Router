package com.emergencyrouter.service;

import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.strategy.FastestRouteStrategy;
import com.emergencyrouter.strategy.ShortestDistanceStrategy;
import junit.framework.TestCase;

/**
 * Tests routing service strategy injection and switching.
 */
public class RoutingServiceTest extends TestCase {
    private final Coordinate start = new Coordinate(32.8872, 13.1913);
    private final Coordinate end = new Coordinate(32.8950, 13.2100);

    /**
     * Verifies the service calculates and stores the current route.
     */
    public void testCalculateRouteStoresCurrentRoute() {
        RoutingService routingService = new RoutingService(new FastestRouteStrategy());

        Route route = routingService.calculateRoute(start, end);

        assertTrue(routingService.getCurrentRoute().isPresent());
        assertSame(route, routingService.getCurrentRoute().get());
    }

    /**
     * Verifies the service can switch route strategies at runtime.
     */
    public void testSetStrategySwitchesRoutingBehavior() {
        RoutingService routingService = new RoutingService(new ShortestDistanceStrategy());
        Route shortest = routingService.calculateRoute(start, end);

        routingService.setStrategy(new FastestRouteStrategy());
        Route fastest = routingService.calculateRoute(start, end);

        assertTrue(routingService.getStrategy() instanceof FastestRouteStrategy);
        assertTrue(fastest.getTime() < shortest.getTime());
    }
}
