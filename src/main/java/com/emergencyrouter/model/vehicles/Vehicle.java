package com.emergencyrouter.model.vehicles;

import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Report;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Abstract base class for all emergency vehicles.
 *
 * <p>Use this class through polymorphism in dispatch code. The dispatch service
 * can work with a {@code Vehicle} list while each subclass provides its own
 * response behavior.</p>
 *
 * <p>Single Responsibility Principle: this class owns only vehicle identity,
 * location, status, and response-related behavior.</p>
 */
public abstract class Vehicle {
    private final String id;
    private Location currentLocation;
    private VehicleStatus status;

    /**
     * Creates a vehicle with a starting location.
     *
     * @param id unique vehicle identifier
     * @param currentLocation current vehicle location
     */
    protected Vehicle(String id, Location currentLocation) {
        this.id = requireText(id, "id");
        this.currentLocation = Objects.requireNonNull(currentLocation, "currentLocation must not be null");
        this.status = VehicleStatus.AVAILABLE;
    }

    /**
     * Responds to an emergency report.
     *
     * <p>Use this method when a dispatch service assigns a vehicle to a report.
     * Subclasses implement the specialized response.</p>
     *
     * @param report emergency report to handle
     */
    public abstract void respondToReport(Report report);

    /**
     * Checks whether this vehicle can handle the report type.
     *
     * <p>Use this method from dispatch logic instead of checking concrete
     * classes with {@code instanceof}. This keeps dispatch open for new vehicle
     * types: a new vehicle only needs to define its own capability here.</p>
     *
     * @param report emergency report to evaluate
     * @return true when this vehicle can respond to the report
     */
    public abstract boolean canHandle(Report report);

    /**
     * Updates the vehicle's current location.
     *
     * <p>Use this method after a vehicle moves, reaches a report, or returns to
     * a station.</p>
     *
     * @param location new vehicle location
     */
    public void updateLocation(Location location) {
        this.currentLocation = Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Checks whether the vehicle can be dispatched.
     *
     * <p>Use this method before assigning a report. It keeps dispatch rules
     * readable and avoids duplicating status checks.</p>
     *
     * @return true when the vehicle status is AVAILABLE
     */
    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }

    /**
     * Gets the vehicle identifier.
     *
     * <p>Use this method in logs, console output, and test assertions.</p>
     *
     * @return vehicle id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current vehicle location.
     *
     * <p>Use this method as the start location when calculating a dispatch
     * route.</p>
     *
     * @return current location
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Gets the current vehicle status.
     *
     * <p>Use this method when displaying fleet state or writing tests around
     * availability.</p>
     *
     * @return vehicle status
     */
    public VehicleStatus getStatus() {
        return status;
    }

    /**
     * Changes the vehicle status.
     *
     * <p>Use this method when a vehicle becomes busy, returns to service, or is
     * taken out of service.</p>
     *
     * @param status new status
     */
    public void setStatus(VehicleStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Marks the vehicle as busy after it accepts a report.
     *
     * <p>Use this helper inside subclasses to keep response implementations
     * consistent.</p>
     */
    protected void markBusy() {
        setStatus(VehicleStatus.BUSY);
    }

    /**
     * Checks whether a report type matches any supported aliases.
     *
     * <p>Use this helper inside subclasses so each vehicle can declare its own
     * supported emergency categories without duplicating normalization logic.</p>
     *
     * @param report emergency report being evaluated
     * @param supportedTypes aliases this vehicle supports
     * @return true when the report type matches one of the aliases
     */
    protected boolean reportTypeMatches(Report report, String... supportedTypes) {
        Objects.requireNonNull(report, "report must not be null");
        String reportType = normalizeEmergencyType(report.getType());

        return Arrays.stream(supportedTypes)
                .map(Vehicle::normalizeEmergencyType)
                .anyMatch(reportType::equals);
    }

    private static String normalizeEmergencyType(String type) {
        if (type == null || type.isBlank()) {
            return "";
        }

        return type.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
