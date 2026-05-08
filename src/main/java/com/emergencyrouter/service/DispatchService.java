package com.emergencyrouter.service;

import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.vehicles.Vehicle;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinates emergency vehicle selection for incoming reports.
 *
 * <p>Use this service when an emergency report arrives and the system needs to
 * find an available vehicle that matches the emergency type.</p>
 *
 * <p>Single Responsibility Principle: this service selects and assigns
 * vehicles; routing calculations stay in routing services and strategies.</p>
 */
public final class DispatchService {
    private final List<Vehicle> vehicles;

    /**
     * Creates a dispatch service with a fleet of vehicles.
     *
     * <p>Use this constructor during setup after vehicles have been created by
     * {@code VehicleFactory}.</p>
     *
     * @param vehicles available fleet list
     */
    public DispatchService(List<Vehicle> vehicles) {
        this.vehicles = List.copyOf(Objects.requireNonNull(vehicles, "vehicles must not be null"));
    }

    /**
     * Assigns a suitable vehicle to a report when one is available.
     *
     * <p>Use this method for the high-level dispatch workflow. It prints a clear
     * message, calls the selected vehicle's polymorphic response method, and
     * handles the no-vehicle case without crashing the program.</p>
     *
     * @param report emergency report to assign
     */
    public void assignVehicle(Report report) {
        Objects.requireNonNull(report, "report must not be null");

        Optional<Vehicle> selectedVehicle = selectVehicle(report);

        if (selectedVehicle.isEmpty()) {
            System.out.println("No suitable available vehicle found for report " + report.getId() + ".");
            return;
        }

        Vehicle vehicle = selectedVehicle.get();
        System.out.println("Available " + vehicle.getClass().getSimpleName() + " selected.");
        vehicle.respondToReport(report);
    }

    /**
     * Finds the first available vehicle suitable for the report.
     *
     * <p>Use this method in tests or services that need to inspect the selected
     * vehicle before assigning it.</p>
     *
     * @param report emergency report to evaluate
     * @return selected vehicle, or empty when none is suitable
     */
    public Optional<Vehicle> selectVehicle(Report report) {
        Objects.requireNonNull(report, "report must not be null");

        return vehicles.stream()
                .filter(Vehicle::isAvailable)
                .filter(vehicle -> isSuitable(vehicle, report))
                .findFirst();
    }

    /**
     * Checks whether a vehicle can handle an emergency report.
     *
     * <p>Use this method to keep the selection loop readable. The actual
     * capability decision belongs to each {@link Vehicle}, so dispatch does not
     * need to know every concrete vehicle class.</p>
     *
     * @param vehicle vehicle being considered
     * @param report emergency report being handled
     * @return true when the vehicle can handle the report type
     */
    public boolean isSuitable(Vehicle vehicle, Report report) {
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        Objects.requireNonNull(report, "report must not be null");

        return vehicle.canHandle(report);
    }
}
