package com.example.model;



public class Ambulance extends Vehicle {

    public Ambulance(String id, Location currentLocation) {
        super(id, currentLocation);
    }

    @Override
    public void respondToReport(Report report) {
        System.out.println("[Ambulance " + id + "] Handling medical emergency: " + report.getId());
        super.respondToReport(report);
    }
}
