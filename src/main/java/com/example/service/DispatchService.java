package com.example.service;

import java.util.ArrayList;
import java.util.List;

import com.example.model.Ambulance;
import com.example.model.FireTruck;
import com.example.model.Location;
import com.example.model.PoliceCar;
import com.example.model.Report;
import com.example.model.Route;
import com.example.model.TrafficData;
import com.example.model.Vehicle;
import com.example.observer.Observer;
import com.example.observer.Subject;

public class DispatchService implements Observer {

    private List<Vehicle> vehicles       = new ArrayList<>();
    private RoutingService routingService = new RoutingService();

    // ── Vehicle management ──────────────────────────────────────────────────

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        System.out.println("[DispatchService] Vehicle added: " + vehicle);
    }

    // ── Core dispatch logic ─────────────────────────────────────────────────

    public void assignVehicle(Report report) {
        System.out.println("\n[DispatchService] Processing report: " + report.getId()
                + " | Type: " + report.getType());

        Vehicle selected = selectVehicle(report);

        if (selected == null) {
            System.out.println("[DispatchService] No available vehicle found for report: " + report.getId());
            return;
        }

        selected.respondToReport(report);

        Route route = routingService.calculateRoute(
                selected.getCurrentLocation(),
                report.getLocation()
        );

        System.out.println("[DispatchService] Route assigned to " + selected.getId() + ": " + route);
    }

    Vehicle selectVehicle(Report report) {
        Vehicle best     = null;
        double  bestDist = Double.MAX_VALUE;

        for (Vehicle v : vehicles) {
            if (!v.isAvailable()) continue;
            if (!isSuitable(v, report)) continue;

            double dist = distance(v.getCurrentLocation(), report.getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                best     = v;
            }
        }

        if (best != null) {
            System.out.println("[DispatchService] Selected vehicle: " + best.getId()
                    + " | Distance: " + Math.round(bestDist * 10.0) / 10.0 + " km");
        }
        return best;
    }

    public boolean isSuitable(Vehicle vehicle, Report report) {
    String type = report.getType().toLowerCase();
    
    if (type.contains("fire")) {
        return vehicle instanceof FireTruck;
    }
    if (type.contains("medical")) {
        return vehicle instanceof Ambulance;
    }
    if (type.contains("security")) {
        return vehicle instanceof PoliceCar;
    }
    return true;
}

    // ── Observer Pattern ────────────────────────────────────────────────────

    public void subscribeToTrafficService(Subject trafficService) {
        trafficService.attach(this);
    }

    @Override
    public void update(TrafficData data) {
        System.out.println("\n[DispatchService] Traffic update received: " + data);
        if (data.isRoadClosed() || data.getCongestionLevel() > 0.7) {
            System.out.println("[DispatchService] Route recalculation may be needed for active missions.");
        }
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private double distance(Location a, Location b) {
        final int R = 6371;
        double dLat = Math.toRadians(b.getLatitude()  - a.getLatitude());
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(a.getLatitude()))
                 * Math.cos(Math.toRadians(b.getLatitude()))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
