package com.emergencyrouter.strategy;

import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.Route;
import junit.framework.TestCase;

/**
 * Tests the basic coordinate-based route strategies.
 */
public class BasicRouteStrategyTest extends TestCase {
    private final Coordinate start = new Coordinate(32.8872, 13.1913);
    private final Coordinate end = new Coordinate(32.8950, 13.2100);

    /**
     * Verifies the fastest strategy returns a valid route.
     */
    public void testFastestRouteStrategyReturnsValidRoute() {
        Route route = new FastestRouteStrategy().calculateRoute(start, end);

        assertEquals(2, route.getPath().size());
        assertTrue(route.getDistance() > 0);
        assertTrue(route.getTime() > 0);
    }

    /**
     * Verifies the shortest strategy returns a valid route.
     */
    public void testShortestDistanceStrategyReturnsValidRoute() {
        Route route = new ShortestDistanceStrategy().calculateRoute(start, end);

        assertEquals(2, route.getPath().size());
        assertTrue(route.getDistance() > 0);
        assertTrue(route.getTime() > 0);
    }

    /**
     * Verifies the fastest route has a lower time estimate because it uses a
     * higher emergency speed assumption.
     */
    public void testFastestRouteHasLowerTimeEstimate() {
        Route fastest = new FastestRouteStrategy().calculateRoute(start, end);
        Route shortest = new ShortestDistanceStrategy().calculateRoute(start, end);

        assertEquals(shortest.getDistance(), fastest.getDistance(), 0.0001);
        assertTrue(fastest.getTime() < shortest.getTime());
    }
}
