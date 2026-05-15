package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.VehicleRoutingStrategy;
import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.service.RoutingService;

public final class RoadVehicleRoutingStrategy
        implements VehicleRoutingStrategy {

    private final RoutingService routingService;

    public RoadVehicleRoutingStrategy(
            RoutingService routingService
    ) {
        this.routingService = routingService;
    }

    @Override
    public Route calculateRoute(
            Vehicle vehicle,
            double lat,
            double lon
    ) {

        return routingService.calculateRoute(
                vehicle.getCurrentLocation(),
                new Coordinate(lat, lon)
        );
    }
}