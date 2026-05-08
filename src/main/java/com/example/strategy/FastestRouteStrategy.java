package com.example.strategy;

import com.example.model.Coordinate;
import com.example.model.Location;
import com.example.model.Route;

import java.util.Arrays;

public class FastestRouteStrategy implements RouteStrategy {

    @Override
    public Route calculateRoute(Location start, Location end) {
        System.out.println("[FastestRouteStrategy] Calculating fastest route...");

        // Haversine formula to calculate real distance in km
        double distance = haversine(start.getLatitude(), start.getLongitude(),
                                    end.getLatitude(),   end.getLongitude());

        // Estimated time assuming avg speed 80 km/h for emergency vehicles
        double time = (distance / 80.0) * 60;

        Route route = new Route(
            Arrays.asList(start, new Coordinate((start.getLatitude()  + end.getLatitude())  / 2,
                                                (start.getLongitude() + end.getLongitude()) / 2), end),
            Math.round(distance * 10.0) / 10.0,
            Math.round(time * 10.0) / 10.0
        );

        System.out.println("[FastestRouteStrategy] Route calculated: " + route);
        return route;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
