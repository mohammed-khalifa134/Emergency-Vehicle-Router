package com.emergencyrouter.controller;

import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.enums.LocationType;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.*;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.service.DispatchService;
import com.emergencyrouter.service.RoutingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class MapController {

    private final RoutingService routingService;

    private final DispatchService dispatchService;

    public MapController(
            RoutingService routingService,
            DispatchService dispatchService
    ) {

        this.routingService = routingService;
        this.dispatchService = dispatchService;
    }

    public DispatchResult createEmergencyReport(
            EmergencyType type,
            double latitude,
            double longitude,
            int severity
    ) {

        MapLocation incidentLocation =
                MapLocation.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .name("حادث")
                        .type(LocationType.INCIDENT)
                        .build();

        Report report =
                Report.builder()
                        .id(UUID.randomUUID().toString())
                        .type(type)
                        .location(incidentLocation)
                        .timestamp(LocalDateTime.now())
                        .severityLevel(severity)
                        .build();

        return dispatchService.dispatch(report);
    }

    public Route calculateVehicleRoute(
            Vehicle vehicle,
            Report report
    ) {

        return routingService.calculateRoute(
                vehicle.getCurrentLocation(),
                report.getLocation()
        );
    }

    public List<Location> getRoutePath(
            Vehicle vehicle,
            Report report
    ) {

        return calculateVehicleRoute(
                vehicle,
                report
        ).getPath();
    }
}