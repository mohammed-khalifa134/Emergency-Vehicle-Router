package com.emergencyrouter.interfaces;

import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.RouteResult;
import com.emergencyrouter.model.vehicles.Vehicle;

public interface VehicleRoutingStrategy {

    Route calculateRoute(
            Vehicle vehicle,
            double lat,
            double lon
    );
}