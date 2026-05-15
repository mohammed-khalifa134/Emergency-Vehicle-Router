package com.emergencyrouter.model.vehicles;

import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Report;

import java.util.Arrays;
import java.util.Objects;

public abstract class Vehicle {

    private final String id;

    private Location currentLocation;

    private VehicleStatus status;

    protected Vehicle(String id, Location currentLocation) {

        this.id = requireText(id, "id");

        this.currentLocation =
                Objects.requireNonNull(currentLocation);

        this.status = VehicleStatus.AVAILABLE;
    }

    public abstract void respondToReport(Report report);

    public abstract boolean canHandle(Report report);

    public void updateLocation(Location location) {

        this.currentLocation =
                Objects.requireNonNull(location);
    }

    public boolean isAvailable() {

        return status == VehicleStatus.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {

        this.status = Objects.requireNonNull(status);
    }

    protected void markBusy() {

        setStatus(VehicleStatus.BUSY);
    }

    protected boolean reportTypeMatches(
            Report report,
            EmergencyType... supportedTypes
    ) {

        Objects.requireNonNull(report);

        return Arrays.stream(supportedTypes)
                .anyMatch(type -> type == report.getType());
    }

    private static String requireText(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value;
    }
}