package com.emergencyrouter.model;

import com.emergencyrouter.model.vehicles.Vehicle;

public record DispatchResult(

        boolean success,

        Vehicle vehicle,

        Report report,

        Route route,

        String message

) {
}