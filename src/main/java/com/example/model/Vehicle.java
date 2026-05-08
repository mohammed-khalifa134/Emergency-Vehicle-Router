package com.example.model;

public abstract class Vehicle {

    protected String id;
    protected Location currentLocation;
    protected VehicleStatus status;

    public Vehicle(String id, Location currentLocation) {
        this.id              = id;
        this.currentLocation = currentLocation;
        this.status          = VehicleStatus.AVAILABLE;
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

    public void respondToReport(Report report) {
        this.status = VehicleStatus.BUSY;
        System.out.println("[" + getClass().getSimpleName() + " " + id + "] Responding to report: " + report.getId()
                + " | Type: " + report.getType());
    }

    public void updateLocation(Location location) {
        this.currentLocation = location;
        System.out.println("[" + getClass().getSimpleName() + " " + id + "] Location updated to: " + location);
    }

    public boolean isAvailable() {
        return this.status == VehicleStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', status=" + status + ", location=" + currentLocation + "}";
    }
}
