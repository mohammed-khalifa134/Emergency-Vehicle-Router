package com.emergencyrouter.model;

import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.interfaces.Location;

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

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
