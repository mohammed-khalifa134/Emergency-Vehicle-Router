package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.VehicleRoutingStrategy;
import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.vehicles.Vehicle;

public final class AirRoutingStrategy
        implements VehicleRoutingStrategy {

    @Override
    public Route calculateRoute(
            Vehicle vehicle,
            double lat,
            double lon
    ) {

        FastestRouteStrategy strategy =
                new FastestRouteStrategy();

        return strategy.calculateRoute(
                vehicle.getCurrentLocation(),
                new Coordinate(lat, lon)
        );
    }
}