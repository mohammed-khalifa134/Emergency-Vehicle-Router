package com.example.model;

public class PoliceCar extends Vehicle {

    public PoliceCar(String id, Location currentLocation) {
        super(id, currentLocation);
    }

    @Override
    public void respondToReport(Report report) {
        System.out.println("[PoliceCar " + id + "] Handling security incident: " + report.getId());
        super.respondToReport(report);
    }
}
