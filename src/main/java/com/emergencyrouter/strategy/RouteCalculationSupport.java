package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;

import java.util.List;

/**
 * Shared helper methods for simple coordinate-based route strategies.
 *
 * <p>Use this package-private class to avoid duplicating distance and route
 * creation logic across basic strategies.</p>
 */
final class RouteCalculationSupport {
    private static final double KILOMETERS_PER_COORDINATE_DEGREE = 111.0;
    private static final double MINUTES_PER_HOUR = 60.0;

    private RouteCalculationSupport() {
        // Utility class: strategy helpers are called statically.
    }

    /**
     * Estimates distance between two latitude/longitude points.
     *
     * <p>Use this method for beginner-friendly coordinate strategies before the
     * graph-based algorithms arrive in later phases. It is a simple educational
     * estimate, not a production GIS calculation.</p>
     *
     * @param start start location
     * @param end destination location
     * @return approximate distance in kilometers
     */
    static double estimateDistanceInKilometers(Location start, Location end) {
        double latitudeDifference = start.getLatitude() - end.getLatitude();
        double longitudeDifference = start.getLongitude() - end.getLongitude();
        double coordinateDistance = Math.sqrt(
                latitudeDifference * latitudeDifference
                        + longitudeDifference * longitudeDifference
        );

        return coordinateDistance * KILOMETERS_PER_COORDINATE_DEGREE;
    }

    /**
     * Converts distance and speed into estimated travel time.
     *
     * <p>Use this method when a route strategy needs a simple time estimate for
     * console output or strategy comparison.</p>
     *
     * @param distanceInKilometers route distance
     * @param averageSpeedKilometersPerHour assumed average speed
     * @return estimated time in minutes
     */
    static double estimateTimeInMinutes(double distanceInKilometers, double averageSpeedKilometersPerHour) {
        if (averageSpeedKilometersPerHour <= 0) {
            throw new IllegalArgumentException("average speed must be greater than zero");
        }

        return distanceInKilometers / averageSpeedKilometersPerHour * MINUTES_PER_HOUR;
    }

    /**
     * Builds a simple two-point route.
     *
     * <p>Use this method for strategies that do not yet have graph paths. Later
     * graph strategies will return richer paths with intermediate nodes.</p>
     *
     * @param start start location
     * @param end destination location
     * @param distance route distance
     * @param time estimated travel time
     * @return route containing start and destination
     */
    static Route buildDirectRoute(Location start, Location end, double distance, double time) {
        return new Route(List.of(start, end), distance, time);
    }
}
