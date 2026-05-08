package com.example.model;

public class FireTruck extends Vehicle {

    public FireTruck(String id, Location currentLocation) {
        super(id, currentLocation);
    }

    @Override
    public void respondToReport(Report report) {
        System.out.println("[FireTruck " + id + "] Handling fire emergency: " + report.getId());
        super.respondToReport(report);
    }
}
