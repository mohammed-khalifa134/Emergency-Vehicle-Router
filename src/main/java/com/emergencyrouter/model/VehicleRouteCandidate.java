package com.emergencyrouter.model;

import com.emergencyrouter.model.vehicles.Vehicle;

import java.util.Objects;

public record VehicleRouteCandidate(

        Vehicle vehicle,

        Route route

) {

    public VehicleRouteCandidate {

        Objects.requireNonNull(
                vehicle,
                "vehicle must not be null"
        );

        Objects.requireNonNull(
                route,
                "route must not be null"
        );
    }
}