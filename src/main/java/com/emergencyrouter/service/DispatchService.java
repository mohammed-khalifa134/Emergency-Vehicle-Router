package com.emergencyrouter.service;

import com.emergencyrouter.enums.ReportStatus;
import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.model.DispatchResult;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.VehicleRouteCandidate;
import com.emergencyrouter.model.vehicles.Helicopter;
import com.emergencyrouter.model.vehicles.Drone;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.strategy.RouteStrategy;
import com.emergencyrouter.strategy.ShortestDistanceStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DispatchService {

    private final List<Vehicle> vehicles;

    private final RoutingService routingService;

    private final RouteStrategy airStrategy;

    public DispatchService(
            List<Vehicle> vehicles,
            RoutingService routingService
    ) {

        this.vehicles = List.copyOf(
                Objects.requireNonNull(vehicles)
        );

        this.routingService =
                Objects.requireNonNull(routingService);

        /*
         * الطائرات والدرون لا تحتاج طرق شوارع
         */
        this.airStrategy =
                new ShortestDistanceStrategy();
    }

    public DispatchResult dispatch(
            Report report
    ) {

        Objects.requireNonNull(report);

        Optional<VehicleRouteCandidate> candidate =
                selectBestVehicle(report);

        if (candidate.isEmpty()) {

            return new DispatchResult(
                    false,
                    null,
                    report,
                    null,
                    "No available vehicle found"
            );
        }

        VehicleRouteCandidate best =
                candidate.get();

        Vehicle vehicle =
                best.vehicle();

        Route route =
                best.route();

        vehicle.setStatus(
                VehicleStatus.BUSY
        );

        report.setStatus(
                ReportStatus.DISPATCHED
        );

        vehicle.respondToReport(report);

        return new DispatchResult(
                true,
                vehicle,
                report,
                route,
                "Vehicle dispatched successfully"
        );
    }

    public Optional<VehicleRouteCandidate>
    selectBestVehicle(
            Report report
    ) {

        Objects.requireNonNull(report);

        return vehicles.stream()

                .filter(Vehicle::isAvailable)

                .filter(vehicle ->
                        vehicle.canHandle(report)
                )

                .map(vehicle -> {

                    Route route =
                            calculateVehicleRoute(
                                    vehicle,
                                    report
                            );

                    return new VehicleRouteCandidate(
                            vehicle,
                            route
                    );
                })

                /*
                 * اختر أسرع مركبة
                 */
                .min(
                        Comparator.comparingDouble(
                                candidate ->
                                        candidate.route().getTime()
                        )
                );
    }

    private Route calculateVehicleRoute(
            Vehicle vehicle,
            Report report
    ) {

        /*
         * الطائرات والدرون
         */
        if (vehicle instanceof Helicopter
                || vehicle instanceof Drone) {

            return airStrategy.calculateRoute(
                    vehicle.getCurrentLocation(),
                    report.getLocation()
            );
        }

        /*
         * المركبات الأرضية
         */
        return routingService.calculateRoute(
                vehicle.getCurrentLocation(),
                report.getLocation()
        );
    }
}
