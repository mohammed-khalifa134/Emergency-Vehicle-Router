package com.emergencyrouter.strategy;



import com.emergencyrouter.interfaces.VehicleRoutingStrategy;
import com.emergencyrouter.model.Route;

import com.emergencyrouter.model.vehicles.Vehicle;

public final class WaterRoutingStrategy
        implements VehicleRoutingStrategy {

    @Override
    public Route calculateRoute(
            Vehicle vehicle,
            double lat,
            double lon
    ) {

        throw new UnsupportedOperationException(
                "Water routing not implemented yet"
        );
    }
}